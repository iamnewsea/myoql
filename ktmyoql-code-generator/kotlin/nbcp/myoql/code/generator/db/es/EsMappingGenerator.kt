package nbcp.myoql.code.generator.db.es

import nbcp.base.comm.JsonMap
import nbcp.base.comm.StringMap
import nbcp.base.db.annotation.*
import nbcp.base.enums.JsonStyleScopeEnum
import nbcp.base.extend.*
import nbcp.base.extend.*
import nbcp.base.extend.*
import nbcp.base.utils.ClassUtil
import nbcp.base.utils.StringUtil
import nbcp.base.utils.WbsNoKeyTouchEnum
import nbcp.myoql.db.comm.DbDefine
import nbcp.myoql.db.comm.IkFieldDefine
import java.io.File
import java.io.FileWriter
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*


/**
 * es mapping 代码生成器
 */
class EsMappingGenerator {
    private var nameMapping: StringMap = StringMap();


    fun work(
        targetFileName: String,  //目标文件
        basePackage: String,   //实体的包名
        nameMapping: StringMap = StringMap(), // 名称转换
        ignoreGroups: List<String> = listOf()  //忽略的包名
    ) {
        this.nameMapping = nameMapping;

        var p = File.separator;

//        var path = Thread.currentThread().contextClassLoader.getResource("").path.split("/target/")[0]
//        var moer_Path = File(path).parentFile.path + "/shop-orm/kotlin/nbcp/db/mongo/mor_tables.kt".replace("/", p);
        var moer_Path = targetFileName.replace("/", p).replace("\\", p);


        File(moer_Path).deleteRecursively();


        var groups = getGroups(basePackage).filter { ignoreGroups.contains(it.key) == false };

        println("开始生成 es mapping ...")

        usingScope(JsonStyleScopeEnum.PRETTY) {
            var count = 0;
            groups.forEach { group ->
                var groupName = group.key
                var groupEntities = group.value

                var path = moer_Path + p + group.key;
                File(path).mkdirs();

                println("${groupName}:")
                groupEntities.forEach {
                    count++;
                    var entType = it;
                    var dbName =
                        entType.getAnnotation(DbName::class.java)?.value.AsString(entType.simpleName)

                    dbName = StringUtil.getKebabCase(dbName)

                    println(
                        "${
                            count.toString().padStart(2, ' ')
                        } 生成Mapping：${groupName}.${dbName}".ToTab(1)
                    )

                    var json = genEntity(it)

                    var mappings = JsonMap("properties" to json)


                    var moer_File = FileWriter(path + p + dbName + ".txt", false);
                    moer_File.appendLine(mappings.ToJson())
                    moer_File.flush()
                }
            }
        }

        println("")
        println("生成 mapping 完成,位置:${moer_Path}")
        println("---")
        println("创建 mapping:")
        println("https://www.elastic.co/guide/en/elasticsearch/reference/7.6/indices-put-mapping.html")
        println("https://www.elastic.co/guide/en/elasticsearch/reference/7.6/mapping.html")
        println("使用 rest index:")
        println("https://www.elastic.co/guide/en/elasticsearch/reference/7.6/rest-apis.html")
        println("https://www.elastic.co/guide/en/elasticsearch/reference/7.6/indices.html")
        println("---")
        println("创建空的 index: curl -X PUT /{index}")
        println("更新Mapping：")
        println("  curl -X PUT -H 'Content-Type: application/json' '/{index}/_mapping'  -d 'json'")
        println("  curl -X PUT -H 'Content-Type: application/json' '/{index}/_mapping'  -d@jsonfile ")
    }

    var maxLevel = 9;


    fun getGroups(basePackage: String): HashMap<String, MutableList<Class<*>>> {
        var ret = HashMap<String, MutableList<Class<*>>>();


        ClassUtil.findClasses(basePackage)
            .filter { it.isAnnotationPresent(DbEntityGroup::class.java) }
            .forEach {

                var groupName = it.getAnnotation(DbEntityGroup::class.java).value;

                if (ret.containsKey(groupName) == false) {
                    ret[groupName] = mutableListOf();
                }

                ret[groupName]!!.add(it)
            }


        return ret
    }


    fun getActType(type: Class<*>): Class<*> {
        if (type.IsSimpleType()) return type
        if (type.isArray) {
            return getActType(type.componentType)
        }
        return type
    }

    fun getActType(field: Field): Class<*> {
        if (field.type.IsSimpleType()) return field.type
        if (field.type.isArray) {
            return getActType(field.type.componentType)
        }
        if (field.type.IsCollectionType) {
            var actType = (field.genericType as ParameterizedType).GetActualClass(0);
            return getActType(actType)
        }

        return field.type
    }

    fun getDefines(entType: Class<*>): Map<String, Any?> {
        var defines = entType.getAnnotationsByType(DbDefine::class.java)

        var ret = defines.map { it.fieldName to it.define }
            .toMap()
            .toMap();


        var ikMap = mapOf<String, String>()
        var ikDefines = entType.getAnnotation(IkFieldDefine::class.java)
        if (ikDefines != null) {
            ikMap =
                ikDefines.fieldNames.map { it to """{"type":"text","index":"true","boost":"1","analyzer":"ik_max_word","search_analyzer":"ik_max_word"}""" }
                    .toMap()
        }

        ret = ikMap + ret

        // 把 key 展开。
        var objKeys = ret.keys.filter { it.contains(".") }.toList();

        objKeys.forEach {
            ret.setValueByWbsPath(it, ret.get(it))
        }

        ret = ret - objKeys;

        return ret;
    }

    /**
     * TODO 优先使用最外层实体的定义，如果外层没有，再使用当前实体的定义。
     */
    fun genEntity(entType: Class<*>, parentDef: Map<String, Any?>? = null): JsonMap {
        var json = JsonMap();
        var dbDefines = getDefines(entType) + (parentDef ?: mapOf());

        var getDef4SimpleType: (Field) -> JsonMap = getDefSelf@{ field ->
            var fieldName = field.name
            var def = dbDefines.get(fieldName);
            if (def != null && def is String) {
                return@getDefSelf def!!.FromJson<JsonMap>()!!;
            }


            var defineJson = JsonMap("type" to getJsType(field.type))
            if (field.type.IsAnyDateOrTimeType) {
                //默认是 strict_date_optional_time||epoch_millis
                //自定义为不记录毫秒
                //strict_date_optional_time 合法格式： 2020-01-01T00:00:00Z , 2020-01-01
                defineJson.put(
                    "format",
                    "strict_date_optional_time||yyyy-MM-dd HH:mm:ss||epoch_second"
                )
            }
            return@getDefSelf defineJson
        }

        entType.AllFields
            .filter { it.name != "Companion" }
            .forEach {
                if (it.getAnnotation(Transient::class.java) != null) {
                    return@forEach
                }

                val type = getActType(it)
                var defineJson = JsonMap();

                if (type.IsSimpleType()) {
                    defineJson = getDef4SimpleType(it)

                    json.put(it.name, defineJson);
                } else {
                    var def = dbDefines.get(it.name);
                    if (def != null && def is String) {
                        throw RuntimeException("${entType.simpleName}.${it.name} 是对象类型，不能单独定义");
                    }

                    var subDef = genEntity(type, def as Map<String,Any?>?)

                    if (subDef.any()) {
                        //https://www.jianshu.com/p/6e3d970c65cd
                        json.put(it.name, JsonMap("type" to "nested", "properties" to subDef))
                    }
                }
            }


        //处理最外层的 IkFieldDefine
        dbDefines.filter { it.key.contains(".") }.forEach {
            json.setValueByWbsPath(it.key, value = it.value, touchType = WbsNoKeyTouchEnum.Error);
        }

        return json;
    }

    private fun getJsType(type: Class<*>): String {
        /*文本有两种： text,keyword*/
        if (type.IsStringType) return "keyword"
        if (type.isEnum) return "keyword"

        //long、integer、short、byte、double、float
        if (type.IsNumberType) {
            if (type == Long::class.java || type == java.lang.Long::class.java) {
                return "long"
            }
            if (type == Int::class.java || type == java.lang.Integer::class.java) {
                return "integer"
            }
            if (type == Short::class.java || type == java.lang.Short::class.java) {
                return "short"
            }
            if (type == Byte::class.java || type == java.lang.Byte::class.java) {
                return "byte"
            }
            if (type == Double::class.java || type == java.lang.Double::class.java) {
                return "double"
            }
            if (type == Float::class.java || type == java.lang.Float::class.java) {
                return "float"
            }

            return "double"
        }
        if (type == LocalDateTime::class.java || type == LocalDate::class.java || type == LocalTime::class.java) {
            return "date"
        }

        if (type == Date::class.java) {
            return "date"
        }

        if (type.IsBooleanType) {
            return "boolean"
        }

        if (type.IsSimpleType()) {
            return "";
        }

        if (type.isArray || type.IsCollectionType) {
            return "nested"
        }

        return "";
    }
}
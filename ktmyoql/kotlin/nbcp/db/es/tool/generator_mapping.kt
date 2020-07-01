package nbcp.db.es.tool

import nbcp.comm.*
import nbcp.utils.*
import nbcp.db.*

import java.io.File
import java.io.FileWriter
import java.lang.RuntimeException
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import kotlin.collections.HashMap

/**
 * es mapping 代码生成器
 */
class generator_mapping {
    private var nameMapping: StringMap = StringMap();


    fun work(targetFileName: String,  //目标文件
             basePackage: String,   //实体的包名
             anyEntityClass: Class<*>,  //任意实体的类名
             nameMapping: StringMap = StringMap(), // 名称转换
             ignoreGroups: List<String> = listOf()  //忽略的包名
    ) {
        this.nameMapping = nameMapping;

        var p = File.separator;

//        var path = Thread.currentThread().contextClassLoader.getResource("").path.split("/target/")[0]
//        var moer_Path = File(path).parentFile.path + "/shop-orm/kotlin/nbcp/db/mongo/mor_tables.kt".replace("/", p);
        var moer_Path = targetFileName.replace("/", p).replace("\\",p);


        File(moer_Path).deleteRecursively();


        var groups = getGroups(basePackage, anyEntityClass).filter { ignoreGroups.contains(it.key) == false };

        println("开始生成 es mapping ...")

        using(JsonStyleEnumScope.Pretty) {
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
                    var dbName = entType.getAnnotation(DbName::class.java)?.value ?: ""

                    if (dbName.isEmpty()) {
                        dbName = MyUtil.getSmallCamelCase(entType.simpleName)
                    }
                    println("${count.toString().padStart(2, ' ')} 生成Mapping：${groupName}.${dbName}".ToTab(1))

                    var json = genEntity(it)

                    var mappings = JsonMap("properties" to json)


                    var moer_File = FileWriter(path + p + dbName + ".txt", false);
                    moer_File.appendln(mappings.ToJson())
                    moer_File.flush()
                }
            }
        }

        println("")
        println("生成 mapping 完成!")
        println("---")
        println("创建 mapping:")
        println("https://www.elastic.co/guide/en/elasticsearch/reference/7.6/indices-put-mapping.html")
        println("https://www.elastic.co/guide/en/elasticsearch/reference/7.6/mapping.html")
        println("使用 rest index:")
        println("https://www.elastic.co/guide/en/elasticsearch/reference/7.6/rest-apis.html")
        println("https://www.elastic.co/guide/en/elasticsearch/reference/7.6/indices.html")
        println("---")
        println("创建空的 index: curl -X PUT /{index}")
        println("更新Mapping： curl -X PUT '/{index}/_mapping' -d '{json}' ")
    }

    var maxLevel = 9;


    fun getGroups(basePackage: String, anyEntityClass: Class<*>): HashMap<String, MutableList<Class<*>>> {
        var ret = HashMap<String, MutableList<Class<*>>>();


        MyUtil.findClasses(basePackage, anyEntityClass)
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
        if (field.type.IsCollectionType()) {
            var actType = (field.genericType as ParameterizedType).GetActualClass(0);
            return getActType(actType)
        }

        return field.type
    }

    fun getDefines(field: Field): Map<String, String> {
        var defines = field.getAnnotation(Defines::class.java)
        if (defines != null) {
            return defines.values.map { it.key to it.value }.toMap()
        }

        var define = field.getAnnotation(Define::class.java)
        if (define != null) return mapOf(define.key to define.value)
        return mapOf()
    }

    fun genEntity(entType: Class<*>, parentDefines: Map<String, String> = mapOf()): JsonMap {
        var json = JsonMap();

        entType.AllFields
                .filter { it.name != "Companion" }
                .forEach {
                    var type = getActType(it);

                    var defines = getDefines(it);
                    var defineJson = JsonMap();

                    if (type.IsSimpleType()) {
                        if (defines.filter { it.key.HasValue }.any()) {
                            throw RuntimeException("简单类型不允许指定key：${it.name},:${defines.filter { it.key.HasValue }.map { it.key }.joinToString(",")}")
                        }

                        var define = defines.filter { it.key.isNullOrEmpty() }.entries.firstOrNull()

                        defineJson = (define?.value ?: "").FromJson<JsonMap>() ?: JsonMap();

                        if (parentDefines.containsKey(it.name)) {
                            if (defineJson.any()) {
                                throw RuntimeException("重复定义key:" + it.name)
                            }
                            defineJson = (parentDefines.get(it.name) ?: "").FromJson<JsonMap>() ?: JsonMap();
                        }


                        if (defineJson.containsKey("type") == false) {
                            defineJson.put("type", getJsType(it.type))
                        }
                    } else {
                        var define = defines.filter { it.key.isNullOrEmpty() }.entries.firstOrNull()

                        if (define != null) {
                            defineJson = (define?.value ?: "").FromJson<JsonMap>() ?: JsonMap();
                        }

                        var theDefines = StringMap(parentDefines
                                .filter { d -> d.key == it.name || d.key.startsWith(it.name + ".") }
                                .map { d -> d.key.substring(it.name.length + 1) to d.value }
                                .toMap())

                        if (theDefines.filter { it.key.isNullOrEmpty() }.any()) {
                            if (defineJson.any()) {
                                throw RuntimeException("重复定义key:" + it.name)
                            }

                            defineJson = JsonMap(theDefines.filter { it.key.isNullOrEmpty() })
                        }

                        if (defineJson.containsKey("type") == false) {
                            defineJson.put("type", "nested")
                        }

                        defineJson.put("properties", genEntity(type, defines
                                .filter { it.key.isNotEmpty() }
                                +
                                theDefines.filter { it.key.isNotEmpty() }
                        ))
                    }


                    json.put(it.name, defineJson)
                }

        return json;
    }

    private fun getJsType(type: Class<*>): String {
        /*文本有两种： text,keyword*/
        if (type.IsStringType()) return "keyword"
        if (type.isEnum) return "keyword"

        //long、integer、short、byte、double、float
        if (type.IsNumberType()) {
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

        if (type.IsBooleanType()) {
            return "bool"
        }

        return "nested"
    }
}
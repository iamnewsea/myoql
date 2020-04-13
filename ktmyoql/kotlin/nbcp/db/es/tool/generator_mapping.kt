package nbcp.db.es.tool

import nbcp.comm.*
import nbcp.utils.*
import nbcp.db.DbEntityGroup

import java.io.File
import java.io.FileWriter
import java.lang.RuntimeException
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import java.lang.reflect.WildcardType
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

        var p = System.getProperty("file.separator");

//        var path = Thread.currentThread().contextClassLoader.getResource("").path.split("/target/")[0]
//        var moer_Path = File(path).parentFile.path + "/shop-orm/kotlin/nbcp/db/mongo/mor_tables.kt".replace("/", p);
        var moer_Path = targetFileName.replace("/", p);


        File(moer_Path).deleteRecursively();


        var groups = getGroups(basePackage, anyEntityClass).filter { ignoreGroups.contains(it.key) == false };

        println("开始生成 es mapping ...")

        var count = 0;
        groups.forEach { group ->
            var groupName = group.key
            var groupEntities = group.value

            var path = moer_Path + p + group.key;
            File(path).mkdirs();

            println("${groupName}:")
            groupEntities.forEach {
                count++;
                println("${count.toString().padStart(2, ' ')} 生成Mapping：${groupName}.${it.simpleName}".ToTab(1))

                var json = genEntity(it)

                var mappings = JsonMap(
                        it.simpleName to JsonMap(
                                "_id" to JsonMap("path" to "id"),
                                "properties" to json
                        ))


                var moer_File = FileWriter(path + p + it.simpleName + ".txt", false);
                moer_File.appendln(mappings.ToJson())
                moer_File.flush()
            }
        }

        println("生成 mor 完成!")
    }

    var maxLevel = 9;

    fun getGroups(basePackage: String, anyEntityClass: Class<*>): HashMap<String, MutableList<Class<*>>> {
        var ret = HashMap<String, MutableList<Class<*>>>();


        MyUtil.findClasses(basePackage, anyEntityClass)
                .filter { it.isAnnotationPresent(DbEntityGroup::class.java) }
                .forEach {

                    var groupName = it.getAnnotation(DbEntityGroup::class.java).group;

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
        if (field.type.IsListType()) {
            var actType = (field.genericType as ParameterizedType).GetActualClass(0);
            return getActType(actType)
        }

        return field.type
    }

    fun genEntity(entType: Class<*>): JsonMap {
        var json = JsonMap();

        entType.AllFields
                .filter { it.name != "Companion" }
                .forEach {
                    var type = getActType(it);

                    var define = JsonMap();
                    if (type.IsSimpleType()) {
                        define = (it.getAnnotationsByType(Define::class.java).firstOrNull()?.value
                                ?: "").FromJson<JsonMap>() ?: JsonMap();

                        if (define.containsKey("type") == false) {
                            define.put("type", getJsType(it.type))
                        }
                    } else {
                        define.put("type", "nested")
                        define.put("properties", genEntity(type))
                    }



                    json.put(it.name, define)
                }

        return json;
    }

    private fun getJsType(type: Class<*>): String {
        /*文本有两种： text,keyword*/
        if (type.IsStringType()) return "keyword"
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
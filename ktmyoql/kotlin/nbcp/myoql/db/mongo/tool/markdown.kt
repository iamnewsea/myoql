package nbcp.myoql.db.mongo.tool

import nbcp.base.comm.StringMap
import nbcp.base.db.annotation.*
import nbcp.base.extend.*
import nbcp.base.utils.ClassUtil
import nbcp.base.utils.MyUtil
import java.io.File
import java.io.FileWriter
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType

/**
 * 文档生成器
 */
class markdown {

    private var nameMapping: StringMap = StringMap();

    private lateinit var moer_File: FileWriter

    fun work(targetFileName: String, basePackage: String, nameMapping: StringMap) {
        this.nameMapping = nameMapping;

        var p = File.separator;

//        var path = Thread.currentThread().contextClassLoader.getResource("").path.split("/target/")[0]
//        var moer_Path = File(path).parentFile.path + "/shop-orm/kotlin/nbcp/db/mongo/mor_tables.kt".replace("/", p);
        var moer_Path = targetFileName.replace("/", p).replace("\\", p);


        File(moer_Path).delete();
        File(moer_Path).createNewFile()

        moer_File = FileWriter(moer_Path, true);

        var groups = getGroups(basePackage);
        var embClasses = getEmbClasses(groups);
        var enums = getEnums(groups);
        enums.addAll(getEnums(mutableMapOf("" to embClasses)));

        println("---------------生成 markdown---------------")
        writeToFile("""
# 实体定义文档

## 使用的枚举
""");
        enums.forEach {
            writeToFile(genEnum(it));
        }


        writeToFile("""

## 关于嵌入的实体（非集合实体，是集合引用到的实体）

""")
        embClasses.forEach {
            writeToFile(genEmbEntity(it));
        }


//        val baseEntities = mutableListOf<Class<*>>()
        groups.forEach { group ->
            var groupName = group.key
            var groupEntities = group.value

            writeToFile("""
### ${MyUtil.getBigCamelCase(groupName)} 模块

""")

            writeToFile("\n")

            groupEntities.forEach {
                writeToFile(genEntity(it))
            }
            writeToFile("""}""")
        }


        writeToFile("""
<style>
body{
    margin: 0 auto;
    font-family: "微软雅黑",仿宋,Arial,sans-serif;
    color: black;
    background-color:white;
    line-height: 1;
    padding: 25px;
}
@media screen and (min-width: 768px) {
    body {
        width: 748px;
        margin: 10px auto;
    }
}

body h1 {
    padding: 8px 10px;
    text-align: center;
    font-size: 32px;
    font-weight: normal;
}

body h2{
    padding: 8px 10px;
    background-color: #555555;
    color: #ffffff;
    font-size: 20px;
    font-weight: normal;
    margin: 120px auto 60px;
}
h2:before{
    border-left: solid 5px wheat;
    display: inline-block;
    content: "";
    padding: 10px 2px;
    margin: 4px 4px -4px;
}

ul, ol {
    padding: 0;
    padding-left: 24px;
    margin: 0;
}
li {
    line-height: 24px;
}
p, ul, ol {
    font-size: 16px;
    line-height: 24px;
}

ol ol, ul ol {
    list-style-type: lower-roman;
}

body table {
  border-collapse:collapse;
  color:#606266;
  border-color: #ebeef5;
}

body table  tr:nth-child(even) {
  background-color:#fafafa;
}
body table th ,body table td {
    min-width:80px;
    border:solid 1px #ebeef5;
    white-space: wrap;
    padding: 12px 6px !important;
}
body table thead th{
  background-color: rgba(202, 232, 234, 0.3);
  border-bottom:solid 1px #ebeef5;
  padding: 10px 6px !important;
}

</style>
""")
    }


    fun writeToFile(msg: String) {
        moer_File.appendLine(msg)
        moer_File.flush()
//        println(msg)
    }

    fun getEntityName(name: String): String {
        var nameValue = name;
        nameMapping.forEach {
            nameValue = nameValue.replace(it.key, it.value)
        }
        return nameValue[0].lowercase() + nameValue.substring(1);
    }

    fun getGroups(basePackage: String): MutableMap<String, MutableList<Class<*>>> {
        var ret = mutableMapOf<String, MutableList<Class<*>>>();

        ClassUtil.getClassesWithAnnotationType(basePackage, DbEntityGroup::class.java)
                .forEach {
                    var groupName = it.getAnnotation(DbEntityGroup::class.java).value;

                    if (ret.containsKey(groupName) == false) {
                        ret[groupName] = mutableListOf();
                    }

                    ret[groupName]!!.add(it)
                }
        return ret
    }

    /**
     * 递归返回嵌入实体。
     */
    @JvmOverloads
    fun findEmbClasses(type: Class<*>, deep: Int = 0): List<Class<*>> {

        if (deep == 6) return listOf();

        var ret = type.AllFields
                .filter {
                    if (it.type.IsSimpleType()) return@filter false;
                    if (Map::class.java.isAssignableFrom(it.type)) {
                        return@filter false;
                    }

                    return@filter true;
                }.map {
                    if (it.type.isArray) {
                        return@map it.type.componentType;
                    }
                    if (List::class.java.isAssignableFrom(it.type)) {
                        return@map (it.genericType as ParameterizedType).GetActualClass(0);

                    }
                    return@map it.type;
                }.filter {
                    if (it.IsSimpleType()) return@filter false;
                    if (Map::class.java.isAssignableFrom(it)) {
                        return@filter false;
                    }

                    return@filter true;
                }
                .distinctBy { it.name }
                .toMutableList()

        var subClasses = mutableListOf<Class<*>>()
        ret.forEach {
            subClasses.addAll(findEmbClasses(it, deep + 1));
        }

        ret.addAll(subClasses);

        return ret.distinctBy { it.name }
    }

    @JvmOverloads
    fun findEnumClasses(type: Class<*>, deep: Int = 0): List<Class<*>> {

        if (deep == 6) return listOf();

        var ret = type.AllFields
                .filter {
                    return@filter it.type.isEnum;
                }.map {
                    return@map it.type;
                }
                .distinctBy { it.name }
                .toMutableList()
        return ret.distinctBy { it.name }
    }

    fun getEmbClasses(groups: MutableMap<String, out List<Class<*>>>): List<Class<*>> {
        return groups.values.Unwind()
                .map {
                    return@map findEmbClasses(it)
                }
                .Unwind()
                .distinctBy { it.name }
                .sortedBy { it.name }
    }

    fun getEnums(groups: MutableMap<String, out List<Class<*>>>): MutableList<Class<*>> {
        var list = mutableListOf<Class<*>>()

        groups.values.forEach {
            it.forEach {
                findEnumClasses(it).forEach {

                    if (list.map { it.name }.contains(it.name) == false) {
                        list.add(it);
                    }

                }
            }
        }

        return list;
    }

    var maxLevel = 9;

    private fun getMetaValue(fieldName: String, fieldType: Class<*>, parentTypeName: String): String {
        if (fieldName == "id") {
            return "_id"
        }
        return fieldName;
    }

    private fun getMetaValue(field: Field, parentTypeName: String, deepth: Int): String {

        if (deepth > maxLevel) {
            writeToFile("-------------------已超过最大深度${field.name}:${field.type.name}-----------------");
            return "";
        }

        var ret = getMetaValue(field.name, field.type, parentTypeName);
        if (ret.HasValue) return ret;


        if (List::class.java.isAssignableFrom(field.type)) {
            var actType = (field.genericType as ParameterizedType).GetActualClass(0);


            var ret = getMetaValue(field.name, actType, parentTypeName);
            if (ret.HasValue) return ret;
        }

        return "----找不到getMetaValue----"
    }


    /**
     * @return key = 字段， value = 是否是基本类型。
     */
    private fun getEntityValue(name: String, type: Class<*>): Pair<String, Boolean> {

        val retTypeIsBasicType = true;

        if (name == "id") {
            return "\"_id\"" to retTypeIsBasicType;
        }

        if (type.IsSimpleType() ||
                Map::class.java.isAssignableFrom(type)) {
            return "\"${name}\"" to retTypeIsBasicType
        }

        if (List::class.java.isAssignableFrom(type) ||
                type.isArray) {
            //应该递归调用自己.
            return "" to retTypeIsBasicType
        }

        return """${type.name.split(".").last()}Meta("${name}")""" to false;
    }


    private fun getEntityValue(field: Field): Pair<String, Boolean> {
        var (ret, retTypeIsBasicType) = getEntityValue(field.name, field.type);
        if (ret.HasValue) return ret to retTypeIsBasicType;


        if (List::class.java.isAssignableFrom(field.type)) {
            var actType = (field.genericType as ParameterizedType).GetActualClass(0);

            var (ret2, retTypeIsBasicType2) = getEntityValue(field.name, actType);
            if (ret2.HasValue) return ret2 to retTypeIsBasicType2;
        } else if (field.type.isArray) {
            var (ret2, retTypeIsBasicType2) = getEntityValue(field.name, field.type.componentType);
            if (ret2.HasValue) return ret2 to retTypeIsBasicType2;
        }

        return "-----找不到getEntityValue-----" to false;
    }

    fun genEnum(entType: Class<*>): String {
        var entTypeName = entType.name.split(".").last();
        if (entTypeName.endsWith("\$Companion")) {
            return "";
        }

        var number_remark = entType.GetEnumNumberField();
        var string_remark = entType.GetEnumStringField();
        var props = entType.GetEnumList()
                .map {
                    var s = mutableListOf<String>();
                    s.add("* ${it.toString()}")

                    if (number_remark != null) {
                        s.add("(" + MyUtil.getValueByWbsPath(it, number_remark.name).AsString() + ")");
                    }
                    if (string_remark != null) {
                        s.add(": " + MyUtil.getValueByWbsPath(it, string_remark.name).AsString());
                    }


                    return@map s.joinToString(" ");
                }

        var entityTypeName = entTypeName;
//        var entityVarName = getEntityName(entTypeName);

        var ent = """#### ${entityTypeName}
${props.joinToString("\n")}
""";
        return ent;
    }

    fun genEmbEntity(entType: Class<*>): String {
        var entTypeName = entType.name.split(".").last();
        if (entTypeName.endsWith("\$Companion")) {
            return "";
        }

        var props = entType.AllFields
                .filter { it.name != "Companion" }
                .map {
                    //TODO 再调试一下
                    var v1 = getMetaValue(it, entTypeName, 1)

                    return@map "| ${it.name} | ${it.type.simpleName} |   |"

                }

        var entityTypeName = entTypeName;
//        var entityVarName = getEntityName(entTypeName);

        var ent = """#### ${entityTypeName}
| 字段名  | 数据类型 | 备注  |
| --- | --- | --- |
${props.joinToString("\n")}
""";
        return ent;
    }

    fun genVarName(entType: Class<*>): String {
        var entTypeName = entType.name.split(".").last();
        if (entTypeName.endsWith("\$Companion")) {
            return "";
        }

        return getEntityName(entTypeName);
    }


    fun genEntity(entType: Class<*>): String {
        var entTypeName = entType.name.split(".").last();
        if (entTypeName.endsWith("\$Companion")) {
            return "";
        }

        var props = entType.AllFields
                .filter { it.name != "Companion" }
                .map {
                    //TODO 需要测试一下，为什么没有用到。
                    var (retValue, retTypeIsBasicType) = getEntityValue(it)
                    return@map "| ${it.name} | ${it.type.simpleName} |   |"
                }

//        var entityTypeName = entTypeName + "Entity"
//        var entityVarName = getEntityName(entTypeName)
        var dbName = entType.getAnnotation(DbName::class.java)?.value ?: ""

        if (dbName.isEmpty()) {
            dbName = MyUtil.getSmallCamelCase(entType.simpleName)
        }

        val ent = """#### 集合 ${dbName}

| 字段名  | 数据类型 | 备注  |
| --- | --- | --- |
${props.joinToString("\n")}

"""

        return ent;
    }
}
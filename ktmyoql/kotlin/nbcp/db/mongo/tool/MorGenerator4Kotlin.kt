package nbcp.db.mongo.tool

import nbcp.comm.*
import nbcp.data.Sys
import nbcp.utils.*
import nbcp.db.*
import nbcp.tool.CodeGeneratorHelper
import java.io.File
import java.io.FileWriter
import java.lang.RuntimeException
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import java.time.LocalDateTime

/**
 * 代码生成器
 */
class MorGenerator4Kotlin {
    private var nameMapping: StringMap = StringMap();

    private fun String.GetSafeKotlinName(): String {
        if (this.isEmpty()) return this;

        return this.split(".").map {
            if (Sys.devKeywords.contains(it)) {
                return@map """`${it}`"""
            }
            return@map it;
        }.joinToString(".")
    }

    private var targetEntityPathName: String = ""

    fun work(
        targetPath: String,  //目标文件
        basePackage: String,   //实体的包名
        packageName: String = "nbcp.db.mongo.table",
        packages: Array<String> = arrayOf(),   //import 包名
        entityFilter: ((Class<*>) -> Boolean) = { true },
        nameMapping: StringMap = StringMap(), // 名称转换
        ignoreGroups: List<String> = listOf("MongoBase")  //忽略的包名
    ) {
        targetEntityPathName = MyUtil.joinFilePath(targetPath, packageName.split(".").joinToString("/"))
        this.nameMapping = nameMapping;

        var p = File.separator;

//        var path = Thread.currentThread().contextClassLoader.getResource("").path.split("/target/")[0]
//        var moer_Path = File(path).parentFile.path + "/shop-orm/kotlin/nbcp/db/mongo/mor_tables.kt".replace("/", p);
//        var moer_Path = targetFileName.replace("/", p).replace("\\", p);


        File(targetEntityPathName).deleteRecursively();
        File(targetEntityPathName).mkdirs()

        var groups = getGroups(basePackage).filter { ignoreGroups.contains(it.key) == false };
        var embClasses = getEmbClasses(groups);

        println("开始生成 mor...")
        var fileHeader = """package ${packageName}

import nbcp.db.*
import nbcp.db.mongo.*
import nbcp.utils.*
import nbcp.comm.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.*
${packages.map { "import " + it }.joinToString(const.line_break)}
"""


        embClasses.forEach {
            writeToFile(it.simpleName + "Meta", fileHeader + genEmbEntity(it));
        }

        var count = 0;
        groups.forEach { group ->
            var groupName = group.key
            var groupEntities = group.value.filter(entityFilter);


            writeToFile(
                "${MyUtil.getBigCamelCase(groupName)}Group",
                fileHeader +
                        """
@Component("mongo.${groupName}")
@MetaDataGroup(DatabaseEnum.Mongo, "${groupName}")
class ${MyUtil.getBigCamelCase(groupName)}Group : IDataGroup {
    override fun getEntities(): Set<BaseMetaData<out Any>> = setOf(${
                            group.value.map { genVarName(it).GetSafeKotlinName() }.joinToString(", ")
                        })
"""
            )
            println("${groupName}:")
            writeToFile(
                "${MyUtil.getBigCamelCase(groupName)}Group",
                groupEntities.map { (genVarEntity(it).ToTab(1)) }.joinToString(const.line_break)
            )

            writeToFile("${MyUtil.getBigCamelCase(groupName)}Group", const.line_break)

            groupEntities.forEach {
                count++;
                println("${count.toString().padStart(2, ' ')} 生成实体：${groupName}.${it.simpleName}".ToTab(1))

                writeToFile("${MyUtil.getBigCamelCase(groupName)}Group", genEntity(it).ToTab(1))
            }
            writeToFile("${MyUtil.getBigCamelCase(groupName)}Group", """}""")
        }

        writeToFile(
            "MoerMetaMap",
            fileHeader +
                    """

fun mongoColumnJoin(vararg args: String): MongoColumnName {
    return MongoColumnName(args.toList().filter { it.HasValue }.joinToString("."))
}


data class MoerMetaMap(val parentPropertyName: String) {
    constructor(vararg args: String): this(args.toList().filter { it.HasValue }.joinToString(".")) {
    }
    
    fun keys(keys: String): String {
        return this.parentPropertyName + "." + keys
    }
}

"""
        )


        writeToFile(
            "readme.md",

                    """
实体生成时间: ${LocalDateTime.now().AsString()}
"""
        )
        println("生成 mor 完成!")
    }

    var maxLevel = 9;


    fun writeToFile(className: String, content: String) {

        FileWriter(MyUtil.joinFilePath(targetEntityPathName, if( className.contains(".") ) className else (  className + ".kt") ), true).use { moer_File ->
            moer_File.appendLine(content)
            moer_File.flush()
        }
    }

    fun getEntityName(name: String): String {
        var nameValue = name;
        nameMapping.forEach {
            nameValue = nameValue.replace(it.key, it.value)
        }
        return nameValue[0].lowercase() + nameValue.substring(1);
    }

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

    /**
     * 递归返回嵌入实体。
     */
    fun findEmbClasses(clazz: Class<*>, deep: Int = 0): List<Class<*>> {
        if (deep == 6) return listOf();
        var ret = clazz.AllFields
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
                    return@map (it.genericType as ParameterizedType).GetActualClass(0, {
                        return@GetActualClass clazz.GetFirstTypeArguments()[0] as Class<*>;
                    })
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

    fun getEmbClasses(groups: Map<String, MutableList<Class<*>>>): List<Class<*>> {
        return groups.values.Unwind()
            .map {
                return@map findEmbClasses(it)
            }
            .Unwind()
            .distinctBy { it.name }
            .sortedBy { it.name }
    }

    private fun getMetaValue(fieldName: String, fieldType: Class<*>, parentTypeName: String): String {

        if (fieldName == "id") {
            return "mongoColumnJoin(this.parentPropertyName, \"_id\")"
        }

        if (fieldType.IsSimpleType()) {
            return "mongoColumnJoin(this.parentPropertyName, \"${fieldName}\")"
        }

        if (fieldType.simpleName == parentTypeName) {
            return "mongoColumnJoin(this.parentPropertyName, \"${fieldName}\") /*:递归类*/"
        }

        if (Map::class.java.isAssignableFrom(fieldType)) {
            return "MoerMetaMap(this.parentPropertyName, \"${fieldName}\") /*:map*/"
        }

        if (fieldType.isArray) {
            return "mongoColumnJoin(this.parentPropertyName, \"${fieldName}\") /*:array*/"
        }

        if (List::class.java.isAssignableFrom(fieldType)) {
            //应该递归.
            return ""
        }

        return """${fieldType.name.split(".").last()}Meta(mongoColumnJoin(this.parentPropertyName, "${fieldName}"))""";
    }

    private fun getMetaValue(field: Field, parentType: Class<*>, parentTypeName: String, deepth: Int): String {

        if (deepth > maxLevel) {
            throw RuntimeException("-------------------已超过最大深度${field.name}:${field.type.name}-----------------");
        }

        val ret = getMetaValue(field.name, field.type, parentTypeName);
        if (ret.HasValue) return ret;


        if (List::class.java.isAssignableFrom(field.type)) {
            val actType = (field.genericType as ParameterizedType).GetActualClass(0, {
                return@GetActualClass parentType.GetFirstTypeArguments()[0] as Class<*>;
            })


            val ret2 = getMetaValue(field.name, actType, parentTypeName);
            if (ret2.HasValue) return ret2;
        }

        return "----找不到getMetaValue----"
    }


    /**
     * @return key = 字段， value = 是否是基本类型。
     */
    private fun getEntityValue(name: String, clazz: Class<*>): Pair<String, Boolean> {

        val retTypeIsBasicType = true;

        if (name == "id") {
            return "\"_id\"" to retTypeIsBasicType;
        }

        if (clazz.IsSimpleType() ||
            Map::class.java.isAssignableFrom(clazz)
        ) {
            return "\"${name}\"" to retTypeIsBasicType
        }

        if (List::class.java.isAssignableFrom(clazz) ||
            clazz.isArray
        ) {
            //应该递归调用自己.
            return "" to retTypeIsBasicType
        }

        return """${clazz.name.split(".").last()}Meta("${name}")""" to false;
    }


    private fun getEntityValue1(field: Field, parentType: Class<*>): Pair<String, Boolean> {
        var (ret, retTypeIsBasicType) = getEntityValue(field.name, field.type);
        if (ret.HasValue) return ret to retTypeIsBasicType;


        if (List::class.java.isAssignableFrom(field.type)) {
            var actType = (field.genericType as ParameterizedType).GetActualClass(0, {
                return@GetActualClass parentType.GetFirstTypeArguments()[0] as Class<*>;
            });

            var (ret2, retTypeIsBasicType2) = getEntityValue(field.name, actType);
            if (ret2.HasValue) return ret2 to retTypeIsBasicType2;
        } else if (field.type.isArray) {
            var (ret2, retTypeIsBasicType2) = getEntityValue(field.name, field.type.componentType);
            if (ret2.HasValue) return ret2 to retTypeIsBasicType2;
        }

        return "-----找不到getEntityValue-----" to false;
    }

    fun genEmbEntity(entType: Class<*>): String {
        var entTypeName = entType.name.split(".").last();
        if (entTypeName.endsWith("\$Companion")) {
            return "";
        }

        var props = entType.AllFields
            .filter { it.name != "Companion" }
            .MoveToFirst { it.name == "name" }.MoveToFirst { it.name == "id" }
            .map {
                var v1 = getMetaValue(it, entType, entTypeName, 1)

                return@map """${CodeGeneratorHelper.getFieldComment(it)}${
                    KotlinCoderUtil.getAnnotationCodes(it.annotations).map { const.line_break + it }.joinToString("")
                }
val ${it.name} = ${v1}""".removeEmptyLine().ToTab(1)
            }

        var entityTypeName = entTypeName;


        var ent =
            """${CodeGeneratorHelper.getEntityComment(entType)}${
                KotlinCoderUtil.getAnnotationCodes(entType.annotations).map { const.line_break + it }.joinToString("")
            }
class ${entityTypeName}Meta(private val parentPropertyName: String) : MongoColumnName() {
    constructor(value: MongoColumnName) : this(value.toString()) {}
${props.map { const.line_break + it }.joinToString(const.line_break)}
    override fun toString(): String {
        return mongoColumnJoin(this.parentPropertyName).toString()
    }
}
"""
        return ent;
    }


    fun genVarName(entType: Class<*>): String {
        var entTypeName = entType.name.split(".").last();
        if (entTypeName.endsWith("\$Companion")) {
            return "";
        }

        return getEntityName(entTypeName);
    }

    private fun genVarEntity(entType: Class<*>): String {
        var entTypeName = entType.name.split(".").last();
        if (entTypeName.endsWith("\$Companion")) {
            return "";
        }

        var entityTypeName = entTypeName + "Entity";
        var entityVarName = getEntityName(entTypeName).GetSafeKotlinName();

        var varTable = entType.getAnnotation(VarTable::class.java)
        var varDb = entType.getAnnotation(VarDatabase::class.java)
        var tailRemark = "";
        if (varTable != null) {
            tailRemark += " (变表)"
        }
        if (varDb != null) {
            tailRemark += " (动态库)"
        }

        var ret = mutableListOf<String>()
        ret.add("")

        ret.add(
            """${CodeGeneratorHelper.getEntityComment(entType, tailRemark)}
val ${entityVarName} get() = ${entityTypeName}();"""
        )


        //可能既变表，又变库。可能两个参数。
        var params = StringMap();

        if (varTable?.value.HasValue) {
            params.put(
                MyUtil.getSmallCamelCase(varTable?.value.AsString("")),
                """"${entityVarName}-${'$'}{${MyUtil.getSmallCamelCase(varTable.value)}}""""
            )
        } else {
            params.put("", "\"\"")
        }

        if (varDb?.value.HasValue) {
            params.put(
                MyUtil.getSmallCamelCase(varDb.value.AsString()),
                MyUtil.getSmallCamelCase(varDb.value.AsString())
            );
        } else {
            params.put("", "\"\"")
        }

        if (params.keys.any { it.HasValue }) {
            ret.add("")

            ret.add(
                """${CodeGeneratorHelper.getEntityComment(entType, tailRemark)}
fun ${entityVarName}(${
                    params.keys.filter { it.HasValue }.map { it + ":String" }.joinToString(", ")
                }) = ${entityTypeName}(${params.values.first()},${params.values.last()});"""
            )
        }

        return ret.joinToString(const.line_break);
    }


    /**
     * 核心，生成一个实体
     */
    fun genEntity(entType: Class<*>): String {

        var entTypeName = entType.name.split(".").last();
        if (entTypeName.endsWith("\$Companion")) {
            return "";
        }

        val props = entType.AllFields
            .filter { it.name != "Companion" }
            .MoveToFirst { it.name == "name" }.MoveToFirst { it.name == "id" }
            .map {

                var (retValue, retTypeIsBasicType) = getEntityValue1(it, entType)

                var pv =
                    """${CodeGeneratorHelper.getFieldComment(it)}${
                        KotlinCoderUtil.getAnnotationCodes(it.annotations).map { const.line_break + it }
                            .joinToString("")
                    } """

                if (retTypeIsBasicType) {
                    return@map """${pv}
val ${it.name} = MongoColumnName(${retValue})""".removeEmptyLine().ToTab(1)
                } else {
                    return@map """${pv}
val ${it.name} = ${retValue}""".removeEmptyLine().ToTab(1)
                }
            }.toSet()

        val entityTypeName = entTypeName + "Entity"
        var dbName = entType.getAnnotation(DbName::class.java)?.value ?: ""

        if (dbName.isEmpty()) {
            dbName = MyUtil.getSmallCamelCase(entType.simpleName)
        }

        val idMethods = mutableSetOf<String>()

        //每一项是 用逗号分隔的主键组合
        val uks = mutableSetOf<String>();



        uks.addAll(CodeGeneratorHelper.getEntityUniqueIndexesDefine(entType))


        uks.forEach { uk ->
            var keys = uk.split(",")

            //检测
            keys.forEach {
                if (entType.GetFieldPath(*it.split(".").toTypedArray()) == null) {
                    throw RuntimeException("${entTypeName} 找不到 ${it} 属性!")
                }
            }


            idMethods.add(
                """
    fun queryBy${
                    keys.map { MyUtil.getBigCamelCase(it) }.joinToString("")
                }(${
                    keys.map {
                        "${MyUtil.getSmallCamelCase(it)}: ${
                            entType.GetFieldPath(
                                *it.split(".").toTypedArray()
                            )!!.type.kotlinTypeName
                        }"
                    }.joinToString(",")
                }): MongoQueryClip<${entityTypeName}, ${entType.name}> {
        return this.query()${keys.map { ".where { it.${it} match ${MyUtil.getSmallCamelCase(it)} }" }.joinToString("")}
    }

    fun deleteBy${
                    keys.map { MyUtil.getBigCamelCase(it) }.joinToString("")
                }(${
                    keys.map {
                        "${MyUtil.getSmallCamelCase(it)}: ${
                            entType.GetFieldPath(
                                *it.split(".").toTypedArray()
                            )!!.type.kotlinTypeName
                        }"
                    }.joinToString(",")
                }): MongoDeleteClip<${entityTypeName}> {
        return this.delete()${keys.map { ".where { it.${it} match ${MyUtil.getSmallCamelCase(it)} }" }.joinToString("")}
    }

    fun updateBy${
                    keys.map { MyUtil.getBigCamelCase(it) }.joinToString("")
                }(${
                    keys.map {
                        "${MyUtil.getSmallCamelCase(it)}: ${
                            entType.GetFieldPath(
                                *it.split(".").toTypedArray()
                            )!!.type.kotlinTypeName
                        }"
                    }.joinToString(",")
                }): MongoUpdateClip<${entityTypeName}, ${entType.name}> {
        return this.update()${keys.map { ".where { it.${it} match ${MyUtil.getSmallCamelCase(it)} }" }.joinToString("")}
    }
"""
            )
        }

        var varTable = entType.getAnnotation(VarTable::class.java);

        var varTableRemark = "";
        if (varTable != null) {
            varTableRemark = " (变表)"
//            varTableCode = """${const.line_break}@VarTable("${varTable.value}")"""
        }
        var varDb = entType.getAnnotation(VarDatabase::class.java)
        if (varDb != null) {
            varTableRemark += " (动态库)"
//            varTableCode = """${const.line_break}@VarDatabase("${varDb.value}")"""
        }


//        var sortNumberAnnotation = entType.getAnnotation(SortNumber::class.java)
//        if (sortNumberAnnotation != null) {
//            sortNumber =
//                """${const.line_break}@SortNumber("${sortNumberAnnotation.field}","${sortNumberAnnotation.groupBy}",${sortNumberAnnotation.step})"""
//        }
//        var logicalDelete = "";
//        var LogicalDeleteAnnotation = entType.getAnnotation(LogicalDelete::class.java)
//        if (LogicalDeleteAnnotation != null) {
//            logicalDelete = """${const.line_break}@LogicalDelete("${LogicalDeleteAnnotation.value}")"""
//        }


        val ent =
            """${
                CodeGeneratorHelper.getEntityComment(
                    entType,
                    varTableRemark
                )
            }${KotlinCoderUtil.getAnnotationCodes(entType.annotations).map { const.line_break + it }.joinToString("")}
class ${entityTypeName}(collectionName: String = "", databaseId: String = "")
    : MongoBaseMetaCollection<${entType.name.GetSafeKotlinName()}>(${entType.name.GetSafeKotlinName()}::class.java, "${dbName}", databaseId) {
${props.map { const.line_break + it }.joinToString(const.line_break)}
${idMethods.joinToString(const.line_break)}
}
"""

        return ent;
    }
}
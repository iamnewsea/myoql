package nbcp.myoql.code.generator.db.es

import nbcp.base.comm.StringMap
import nbcp.base.comm.const
import nbcp.base.db.annotation.*
import nbcp.base.extend.*
import nbcp.base.utils.*
import nbcp.myoql.code.generator.tool.*
import java.io.File
import java.io.FileWriter
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import java.time.LocalDateTime

/**
 * 代码生成器
 */
class EsrGenerator4Kotlin {
    private var nameMapping: StringMap = StringMap();


    private var targetEntityPathName: String = ""
    fun work(
        targetPath: String,  //目标文件
        basePackage: String,   //实体的包名
        metaPackageName: String,
        packages: Array<String> = arrayOf(),   //import 包名
        entityFilter: ((Class<*>) -> Boolean) = { true },
        nameMapping: StringMap = StringMap(), // 名称转换
        ignoreGroups: List<String> = listOf("EsBase")  //忽略的包名
    ) {
        targetEntityPathName = FileUtil.resolvePath(targetPath, metaPackageName.split(".").joinToString("/"))
        this.nameMapping = nameMapping;

        var p = File.separator;

        File(targetEntityPathName).deleteRecursively();
        File(targetEntityPathName).mkdirs()

        var groups = getGroups(basePackage).filter { ignoreGroups.contains(it.key) == false };
        var embClasses = getEmbClasses(groups);

        println("开始生成 esr...")


        var fileHeader =  """package ${metaPackageName}

import java.io.*
import nbcp.myoql.db.*
import nbcp.myoql.db.es.*
import nbcp.myoql.db.comm.*
import nbcp.myoql.db.enums.*
import nbcp.myoql.db.es.component.*
import nbcp.base.utils.*
import nbcp.base.extend.*
import nbcp.myoql.db.es.base.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.*
${packages.map { "import " + it }.joinToString(const.line_break)}

"""

        embClasses.forEach {
            writeToFile(it.simpleName + "Meta", fileHeader + genEmbEntity(it));
        }

        var count = 0;
        groups.forEach { group ->
            var groupName = group.key
            var groupEntities = group.value.filter(entityFilter)

            writeToFile(
                "${StringUtil.getBigCamelCase(groupName)}Group",
                fileHeader +
                    """
@Component("es.${groupName}")
@MetaDataGroup(DatabaseEnum.ELASTIC_SEARCH, "${groupName}")
class ${StringUtil.getBigCamelCase(groupName)}Group : IDataGroup{
    override fun getEntities():Set<BaseMetaData<out Any>> = setOf(${group.value.map { genVarName(it) }.joinToString(",")})
"""
            )
            println("${groupName}:")
            groupEntities.forEach {
                count++;

                var dbName = it.getAnnotation(DbName::class.java)?.value.AsString(it.simpleName)

                dbName = StringUtil.getKebabCase(dbName)

                println("${count.toString().padStart(2, ' ')} 生成实体：${groupName}.${dbName}".ToTab(1))
                writeToFile("${StringUtil.getBigCamelCase(groupName)}Group", genVarEntity(it).ToTab(1))
            }

            writeToFile("${StringUtil.getBigCamelCase(groupName)}Group","\n")

            groupEntities.forEach {
                writeToFile("${StringUtil.getBigCamelCase(groupName)}Group",genEntity(it).ToTab(1))
            }
            writeToFile("${StringUtil.getBigCamelCase(groupName)}Group","""}""")
        }

        writeToFile(
            "EsrMetaMap",
            fileHeader +

                """

fun esColumnJoin(vararg args:String): EsColumnName{
    return EsColumnName( args.toList().filter{it.HasValue}.joinToString (".") )
}

data class EsrMetaMap(val parentPropertyName:String) {
    constructor(vararg args: String): this(args.toList().filter { it.HasValue }.joinToString(".")) {
    }
    
    fun keys(keys:String):String{
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
        println("生成 esr 完成!")
    }

    var maxLevel = 9;


    fun writeToFile(className: String, content: String) {

        FileWriter(FileUtil.resolvePath(targetEntityPathName, if( className.contains(".") ) className else (  className + ".kt") ), true).use { moer_File ->
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
                        return@map (it.genericType as ParameterizedType).GetActualClass(0, {
                            return@GetActualClass type.GetFirstTypeArguments()[0] as Class<*>;
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
            return "esColumnJoin(this.parentPropertyName, \"_id\")"
        }

        if (fieldType.IsSimpleType()) {
            return "esColumnJoin(this.parentPropertyName, \"${fieldName}\")"
        }

        if (fieldType.simpleName == parentTypeName) {
            return "esColumnJoin(this.parentPropertyName, \"${fieldName}\") /*:递归类*/"
        }

        if (Map::class.java.isAssignableFrom(fieldType)) {
            return "EsrMetaMap(this.parentPropertyName, \"${fieldName}\") /*:map*/"
        }

        if (fieldType.isArray) {
            return "esColumnJoin(this.parentPropertyName, \"${fieldName}\") /*:array*/"
        }

        if (List::class.java.isAssignableFrom(fieldType)) {
            //应该递归.
            return ""
        }

        return """${fieldType.name.split(".").last()}Meta(esColumnJoin(this.parentPropertyName,"${fieldName}"))""";
    }

    private fun getMetaValue(field: Field, parentType: Class<*>, parentTypeName: String, deepth: Int): String {

        if (deepth > maxLevel) {
            throw RuntimeException("-------------------已超过最大深度${field.name}:${field.type.name}-----------------");
        }

        var ret = getMetaValue(field.name, field.type, parentTypeName);
        if (ret.HasValue) return ret;


        if (List::class.java.isAssignableFrom(field.type)) {
            var actType = (field.genericType as ParameterizedType).GetActualClass(0, {
                return@GetActualClass parentType.GetFirstTypeArguments()[0] as Class<*>;
            })


            var v = getMetaValue(field.name, actType, parentTypeName);
            if (v.HasValue) return v;
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
                Map::class.java.isAssignableFrom(type)
        ) {
            return "\"${name}\"" to retTypeIsBasicType
        }

        if (List::class.java.isAssignableFrom(type) ||
                type.isArray
        ) {
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

    fun genEmbEntity(entType: Class<*>): String {
        var entTypeName = entType.name.split(".").last();
        if (entTypeName.endsWith("\$Companion")) {
            return "";
        }

        var props = entType.AllFields
                .filter { it.name != "Companion" }
                .map {
                    var v1 = getMetaValue(it, entType, entTypeName, 1)

                    return@map "val ${it.name}=${v1}".ToTab(1)
                }

        var entityTypeName = entTypeName;
//        var entityVarName = getEntityName(entTypeName);

        var ent = """class ${entityTypeName}Meta (private val parentPropertyName:String):EsColumnName() {
    constructor(_val:EsColumnName):this(_val.toString()) {}

${props.joinToString("\n")}

    override fun toString(): String {
        return esColumnJoin(this.parentPropertyName).toString()
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
        var entityVarName = getEntityName(entTypeName);

        return """val ${entityVarName} get() =${entityTypeName}();
fun ${entityVarName}(collectionName:String)=${entityTypeName}(collectionName);""";
    }


    fun genEntity(entType: Class<*>): String {

        var entTypeName = entType.name.split(".").last();
        if (entTypeName.endsWith("\$Companion")) {
            return "";
        }
        var props = entType.AllFields
                .filter { it.name != "Companion" }
                .map {

                    var (retValue, retTypeIsBasicType) = getEntityValue(it)
                    if (retTypeIsBasicType) {
                        return@map "val ${it.name} = EsColumnName(${retValue})".ToTab(1)
                    } else {
                        return@map "val ${it.name} = ${retValue}".ToTab(1)
                    }
                }


        var entityTypeName = entTypeName + "Entity"
//        var entityVarName = getEntityName(entTypeName)

        var dbName = entType.getAnnotation(DbName::class.java)?.value.AsString(entType.simpleName)
        dbName = StringUtil.getKebabCase(dbName)


        var idMethods = mutableListOf<String>()

        //每一项是 用逗号分隔的主键组合
        var uks = mutableListOf<String>();

        uks.addAll(FreemarkerUtil.getEntityUniqueIndexesDefine(entType))

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
                        keys.map { StringUtil.getBigCamelCase(it) }.joinToString("")
                    } (${
                        keys.map {
                            "${StringUtil.getSmallCamelCase(it)}: ${
                                entType.GetFieldPath(
                                        *it.split(".").toTypedArray()
                                )!!.type.kotlinTypeName
                            }"
                        }.joinToString(",")
                    }): EsQueryClip<${entityTypeName}, ${entType.name}> {
        return this.query()${keys.map { ".where{ it.${it} match ${StringUtil.getSmallCamelCase(it)} }" }.joinToString("")}
    }

    fun deleteBy${
                        keys.map { StringUtil.getBigCamelCase(it) }.joinToString("")
                    } (${
                        keys.map {
                            "${StringUtil.getSmallCamelCase(it)}: ${
                                entType.GetFieldPath(
                                        *it.split(".").toTypedArray()
                                )!!.type.kotlinTypeName
                            }"
                        }.joinToString(",")
                    }): EsDeleteClip<${entityTypeName}> {
        return this.delete()${keys.map { ".where{ it.${it} match ${StringUtil.getSmallCamelCase(it)} }" }.joinToString("")}
    }

    fun updateBy${
                        keys.map { StringUtil.getBigCamelCase(it) }.joinToString("")
                    } (${
                        keys.map {
                            "${StringUtil.getSmallCamelCase(it)}: ${
                                entType.GetFieldPath(
                                        *it.split(".").toTypedArray()
                                )!!.type.kotlinTypeName
                            }"
                        }.joinToString(",")
                    }): EsUpdateClip<${entityTypeName}> {
        return this.update()${keys.map { ".where{ it.${it} match ${StringUtil.getSmallCamelCase(it)} }" }.joinToString("")}
    }
"""
            )
        }

        val ent = """${
            CnAnnotationUtil.getComment(
                    entType 
            )
        }${KotlinCoderUtil.getAnnotationCodes(entType.annotations).map { const.line_break + it }.joinToString("")}
class ${entityTypeName}(collectionName:String="")
    :EsBaseMetaEntity<${entType.name}>(${entType.name}::class.java, "${dbName}") {
${props.joinToString("\n")}
${idMethods.joinToString("\n")}
}
"""

        return ent;
    }
}
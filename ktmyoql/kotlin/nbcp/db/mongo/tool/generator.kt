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
class generator {
    private var nameMapping: StringMap = StringMap();

    private lateinit var moer_File: FileWriter

    private fun String.GetSafeKotlinName(): String {
        if (this.isEmpty()) return this;

        return this.split(".").map {
            if (Sys.devKeywords.contains(it)) {
                return@map """`${it}`"""
            }
            return@map it;
        }.joinToString(".")
    }

    fun work(
        targetFileName: String,  //目标文件
        basePackage: String,   //实体的包名
        anyEntityClass: Class<*>,  //任意实体的类名
        packages: Array<String> = arrayOf(),   //import 包名
        nameMapping: StringMap = StringMap(), // 名称转换
        ignoreGroups: List<String> = listOf("MongoBase")  //忽略的包名
    ) {
        this.nameMapping = nameMapping;

        var p = File.separator;

//        var path = Thread.currentThread().contextClassLoader.getResource("").path.split("/target/")[0]
//        var moer_Path = File(path).parentFile.path + "/shop-orm/kotlin/nbcp/db/mongo/mor_tables.kt".replace("/", p);
        var moer_Path = targetFileName.replace("/", p).replace("\\", p);


        File(moer_Path).delete();
        File(moer_Path).createNewFile()

        moer_File = FileWriter(moer_Path, true);

        var groups = getGroups(basePackage, anyEntityClass).filter { ignoreGroups.contains(it.key) == false };
        var embClasses = getEmbClasses(groups);

        println("开始生成 mor...")

        writeToFile(
            """package nbcp.db.mongo.table

import nbcp.db.*
import nbcp.db.mongo.*
import nbcp.utils.*
import nbcp.comm.*
import nbcp.db.mongo.entity.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
${packages.map { "import" + it }.joinToString("\n")}

//generate auto @${LocalDateTime.now().AsString()}
"""
        )
        embClasses.forEach {
            writeToFile(genEmbEntity(it));
        }

        var count = 0;
        groups.forEach { group ->
            var groupName = group.key
            var groupEntities = group.value

            writeToFile(
                """
@Component("mongo.${groupName}")
@MetaDataGroup("${groupName}")
class ${MyUtil.getBigCamelCase(groupName)}Group : IDataGroup{
    override fun getEntities():Set<BaseMetaData> = setOf(${
                    group.value.map { genVarName(it).GetSafeKotlinName() }.joinToString(",")
                })
"""
            )
            println("${groupName}:")
            groupEntities.forEach {
                writeToFile(genVarEntity(it).ToTab(1))
            }

            writeToFile("\n")

            groupEntities.forEach {
                count++;
                println("${count.toString().padStart(2, ' ')} 生成实体：${groupName}.${it.simpleName}".ToTab(1))

                writeToFile(genEntity(it).ToTab(1))
            }
            writeToFile("""}""")
        }

        writeToFile(
            """


private fun join(vararg args:String): MongoColumnName{
    return MongoColumnName( args.toList().filter{it.HasValue}.joinToString (".") )
}

private fun join_map(vararg args:String):moer_map{
    return moer_map(args.toList().filter{it.HasValue}.joinToString ("."))
}

data class moer_map(val _pname:String)
{
    fun keys(keys:String):String{
        return this._pname + "." + keys
    }
}

"""
        )

        println("生成 mor 完成!")
    }

    var maxLevel = 9;


    fun writeToFile(msg: String) {
        moer_File.appendln(msg)
        moer_File.flush()
    }

    fun getEntityName(name: String): String {
        var name = name;
        nameMapping.forEach {
            name = name.replace(it.key, it.value)
        }
        return name[0].toLowerCase() + name.substring(1);
    }

    fun getGroups(basePackage: String, anyEntityClass: Class<*>): HashMap<String, MutableList<Class<*>>> {
        var ret = HashMap<String, MutableList<Class<*>>>();


        ClassUtil.findClasses(basePackage, anyEntityClass)
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

    fun getEmbClasses(groups: Map<String, MutableList<Class<*>>>): MutableList<Class<*>> {
        var list = mutableListOf<Class<*>>()

        groups.values.forEach {
            it.forEach {
                findEmbClasses(it).forEach {

                    if (list.map { it.name }.contains(it.name) == false) {
                        list.add(it);
                    }

                }
            }

        }

        return list;
    }

    private fun getMetaValue(fieldName: String, fieldType: Class<*>, parentTypeName: String): String {


        if (fieldName == "id") {
            return "join(this._pname, \"_id\")"
        }

        if (fieldType.IsSimpleType()) {
            return "join(this._pname, \"${fieldName}\")"
        }

        if (fieldType.simpleName == parentTypeName) {
            return "join(this._pname, \"${fieldName}\") /*:递归类*/"
        }

        if (Map::class.java.isAssignableFrom(fieldType)) {
            return "join_map(this._pname, \"${fieldName}\")/*:map*/"
        }

        if (fieldType.isArray) {
            return "join(this._pname, \"${fieldName}\")/*:array*/"
        }

        if (List::class.java.isAssignableFrom(fieldType)) {
            //应该递归.
            return ""
        }

        return """${fieldType.name.split(".").last()}Meta(join(this._pname,"${fieldName}"))""";
    }

    private fun getMetaValue(field: Field, parentType: Class<*>, parentTypeName: String, deepth: Int): String {

        if (deepth > maxLevel) {
            writeToFile("-------------------已超过最大深度${field.name}:${field.type.name}-----------------");
            return "";
        }

        var ret = getMetaValue(field.name, field.type, parentTypeName);
        if (ret.HasValue) return ret;


        if (List::class.java.isAssignableFrom(field.type)) {
            var actType = (field.genericType as ParameterizedType).GetActualClass(0, {
                return@GetActualClass parentType.GetFirstTypeArguments()[0] as Class<*>;
            })


            var ret = getMetaValue(field.name, actType, parentTypeName);
            if (ret.HasValue) return ret;
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

                return@map "${CodeGeneratorHelper.getFieldComment(it)}val ${it.name}=${v1}".ToTab(1)
            }

        var entityTypeName = entTypeName;


        var ent =
            """${CodeGeneratorHelper.getEntityComment(entType)}class ${entityTypeName}Meta (private val _pname:String):MongoColumnName() {
    constructor(_val:MongoColumnName):this(_val.toString()) {}

${props.joinToString("\n")}

    override fun toString(): String {
        return join(this._pname).toString()
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

        return """${CodeGeneratorHelper.getEntityCommentOnly(entType)}val ${entityVarName.GetSafeKotlinName()} get() = ${entityTypeName}();"""
//fun ${entityVarName}(collectionName:String)=${entityTypeName}(collectionName);""";
    }


    /**
     * 核心，生成一个实体
     */
    fun genEntity(entType: Class<*>): String {

        var entTypeName = entType.name.split(".").last();
        if (entTypeName.endsWith("\$Companion")) {
            return "";
        }

        var pks = mutableSetOf<String>()

        var props = entType.AllFields
            .filter { it.name != "Companion" }
            .MoveToFirst { it.name == "name" }.MoveToFirst { it.name == "id" }
            .map {

                if (it.getAnnotation(DbKey::class.java) != null) {
                    pks.add(it.name);
                }

                var (retValue, retTypeIsBasicType) = getEntityValue1(it, entType)
                if (retTypeIsBasicType) {
                    return@map "${CodeGeneratorHelper.getFieldComment(it)}val ${it.name}=MongoColumnName(${retValue})".ToTab(
                        1
                    )
                } else {
                    return@map "${CodeGeneratorHelper.getFieldComment(it)}val ${it.name}=${retValue}".ToTab(1)
                }
            }

        var entityTypeName = entTypeName + "Entity"
        var dbName = entType.getAnnotation(DbName::class.java)?.value ?: ""

        if (dbName.isEmpty()) {
            dbName = MyUtil.getSmallCamelCase(entType.simpleName)
        }

        var idMethods = mutableListOf<String>()

        //每一项是 用逗号分隔的主键组合
        var uks = mutableSetOf<String>();
        if (pks.any()) {
            uks.add(pks.joinToString(","))
        }


        uks.addAll(getUks(entType))


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
                } (${
                    keys.map {
                        "${MyUtil.getSmallCamelCase(it)}: ${
                            entType.GetFieldPath(
                                *it.split(".").toTypedArray()
                            )!!.type.kotlinTypeName
                        }"
                    }.joinToString(",")
                }): MongoQueryClip<${entityTypeName}, ${entType.name}> {
        return this.query()${keys.map { ".where{ it.${it} match ${MyUtil.getSmallCamelCase(it)} }" }.joinToString("")}
    }

    fun deleteBy${
                    keys.map { MyUtil.getBigCamelCase(it) }.joinToString("")
                } (${
                    keys.map {
                        "${MyUtil.getSmallCamelCase(it)}: ${
                            entType.GetFieldPath(
                                *it.split(".").toTypedArray()
                            )!!.type.kotlinTypeName
                        }"
                    }.joinToString(",")
                }): MongoDeleteClip<${entityTypeName}> {
        return this.delete()${keys.map { ".where{ it.${it} match ${MyUtil.getSmallCamelCase(it)} }" }.joinToString("")}
    }

    fun updateBy${
                    keys.map { MyUtil.getBigCamelCase(it) }.joinToString("")
                } (${
                    keys.map {
                        "${MyUtil.getSmallCamelCase(it)}: ${
                            entType.GetFieldPath(
                                *it.split(".").toTypedArray()
                            )!!.type.kotlinTypeName
                        }"
                    }.joinToString(",")
                }): MongoUpdateClip<${entityTypeName}> {
        return this.update()${keys.map { ".where{ it.${it} match ${MyUtil.getSmallCamelCase(it)} }" }.joinToString("")}
    }
"""
            )
        }

        var ent = """${CodeGeneratorHelper.getEntityComment(entType)}class ${entityTypeName}(collectionName:String="")
    :MongoBaseMetaCollection<${entType.name.GetSafeKotlinName()}>(${entType.name.GetSafeKotlinName()}::class.java,collectionName.AsString("${dbName}")) {
${props.joinToString("\n")}
${idMethods.joinToString("\n")}
}
"""

        return ent;
    }


    /**
     * 获取类及父类的注解 DbUks
     */
    private fun getUks(entType: Class<*>, procedClasses: MutableSet<String> = mutableSetOf()): Set<String> {
        procedClasses.add(entType.name)

        var uks = mutableSetOf<String>()
        var uks_define = entType.getAnnotation(DbUks::class.java)
        if (uks_define != null) {
            uks.addAll(uks_define.value)
        }
        if (entType.superclass != null && !procedClasses.contains(entType.superclass.name)) {
            uks.addAll(getUks(entType.superclass, procedClasses))
        }
        return uks;
    }
}
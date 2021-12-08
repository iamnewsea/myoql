package nbcp.db.mysql.tool

import nbcp.comm.*
import nbcp.data.Sys
import nbcp.utils.*
import nbcp.db.*
import nbcp.db.sql.*
import java.io.File
import java.io.FileWriter
import java.lang.RuntimeException
import java.lang.reflect.ParameterizedType
import java.time.LocalDateTime


private fun _join(vararg args: String): String {
    return args.toList().filter { it.HasValue }.joinToString(".");
}

fun <T : SqlColumnName> T.and(vararg value: String): T {
    if (value.any() == false) return this;

    var con = this.javaClass.getConstructor(String::class.java);
    return con.newInstance(_join(MyUtil.getPrivatePropertyValue(this, "_pname").toString(), *value)) as T;
}

/**
 * Created by udi on 17-6-11.
 * MySql 代码生成器
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
            targetFileName: String, //目标文件
            basePackage: String,    //实体的包名
            anyEntityClass: Class<*>, //任意实体的类名
            entityFilter: ((Class<*>) -> Boolean) = { true },
            nameMapping: StringMap = StringMap(), // 名称转换
            ignoreGroups: List<String> = listOf("MongoBase")  //忽略的包名
    ) {
        this.nameMapping = nameMapping
        var p = File.separator;

//        var path = Thread.currentThread().contextClassLoader.getResource("").path.split("/target/")[0]
//        var moer_Path = File(path).parentFile.path + "/shop-orm/kotlin/nbcp/db/mysql/dbr_tables.kt".replace("/", p);
        var moer_Path = targetFileName.replace("/", p).replace("\\", p);

        File(moer_Path).delete();
        File(moer_Path).createNewFile()

        moer_File = FileWriter(moer_Path, true);
        var groups = getGroups(basePackage, anyEntityClass).filter { ignoreGroups.contains(it.key) == false };


        println("---------------生成 dbr---------------")

        writeToFile(
                """package nbcp.db.sql.table

import nbcp.db.*
import nbcp.db.sql.*
import nbcp.db.sql.entity.*
import nbcp.db.mysql.*
import nbcp.db.mysql.entity.*
import nbcp.comm.*
import nbcp.utils.*
import org.springframework.stereotype.Component

//generate auto @${LocalDateTime.now().AsString()}
"""
        )
        var count = 0;

        var exts = mutableListOf<String>();

        groups.forEach { group ->
            var groupEntitys = group.value.filter(entityFilter);
            if (groupEntitys.any() == false) {
                return@forEach
            }


            writeToFile(
                    """
@Component("sql.${group.key}")
@MetaDataGroup("${group.key}")
class ${MyUtil.getBigCamelCase(group.key)}Group : IDataGroup{
    override fun getEntities():Set<BaseMetaData> = setOf(${
                        groupEntitys.map { genVarName(it).GetSafeKotlinName() }.joinToString(",")
                    })
"""
            )
            println("${group.key}:")
            groupEntitys
                    .forEach { entityType ->
                        count++;
                        println("${count.toString().padStart(2, ' ')} 生成实体：${group.key}.${entityType.simpleName}".ToTab(1))
                        writeToFile(genVarEntity(entityType).ToTab(1))
                    }

            writeToFile("\n")

            groupEntitys
                    .forEach { entityType ->
                        var item = genEntity(MyUtil.getSmallCamelCase(group.key), entityType)

                        if (item.ext.HasValue) {
                            exts.add(item.ext);
                        }
                        writeToFile(item.body.ToTab(1))
                    }

            writeToFile("""}""")
        }

        writeToFile(exts.joinToString("\n"))
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

        var ret = clazz.AllFields.filter {
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
                });
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

    fun getEmbClasses(groups: HashMap<String, MutableList<Class<*>>>): MutableList<Class<*>> {
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

    fun getEntityClassName(entTypeName: String): String {
        if (entTypeName.contains('_')) {
            return entTypeName + "_table"
        }

        return entTypeName + "Table"
    }

    fun genVarName(entType: Class<*>): String {
        var entTypeName = entType.name.split(".").last();
        if (entTypeName.endsWith("\$Companion")) {
            return "";
        }

        return getEntityName(entTypeName);
    }

    fun genVarEntity(entType: Class<*>): String {
        var entTypeName = entType.name.split(".").last();
        if (entTypeName.endsWith("\$Companion")) {
            return "";
        }

        var entityVarName = getEntityName(entTypeName);

        return """val ${entityVarName.GetSafeKotlinName()} get() = ${getEntityClassName(entTypeName)}();""";
    }

    class EntityResult {
        var body = ""
        var ext = "";
    }

    fun genEntity(groupName: String, entType: Class<*>): EntityResult {
        val tableName = entType.name.split(".").last();
        if (tableName.endsWith("\$Companion")) {
            return EntityResult();
        }

//        var autoIncrementKey = "";
//        val uks = mutableSetOf<String>()
//        val fks = mutableSetOf<FkDefine>()
//        val pks = mutableListOf<String>()
//        val columns = mutableListOf<String>()
//        val columns_spread = mutableListOf<String>()
//        val columns_convertValue = mutableListOf<String>()
//
//        val props = mutableListOf<String>();
//        val extMethods = mutableListOf<String>()
//        val entityTypeName = getEntityClassName(tableName)
        val idMethods = mutableListOf<String>()

        val entityName = entType.name.split(".").last();
        val entityTableMetaName = getEntityClassName(entityName)
        val columnMetaDefines = getColumnMetaDefines(groupName, entityName, entType);


//        kotlin.run {
//            var rks_define = entType.getAnnotation(SqlRks::class.java)
//            if (rks_define != null) {
//                rks.addAll(rks_define.rkColumns)
//            }
//        }


        var uks2 =
                columnMetaDefines.uks.map { """ arrayOf(${it.split(",").map { "\"" + it + "\"" }.joinToString(",")}) """ }
//        var rks2 = rks.map { """ arrayOf(${it.split(",").map { "\"" + it + "\"" }.joinToString(",")}) """ }
        var fks_exp_string =
                columnMetaDefines.fks.map { """FkDefine("${it.table}","${it.column}","${it.refTable}","${it.refColumn}") """ }
                        .toTypedArray()


        columnMetaDefines.uks.forEach { uk ->
            var keys = uk.split(",")
            //检测
            keys.forEach {
                if (entType.GetFieldPath(*it.split(".").toTypedArray()) == null) {
                    throw RuntimeException("${tableName} 找不到 ${it} 属性!")
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
                    }): SqlQueryClip<${entityTableMetaName}, ${entType.name}> {
        return this.query()${
                        keys.map { ".where{ it.${it.replace(".", "_")} match ${MyUtil.getSmallCamelCase(it)} }" }
                                .joinToString("")
                    }
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
                    }): SqlDeleteClip<${entityTableMetaName}> {
        return this.delete()${
                        keys.map { ".where{ it.${it.replace(".", "_")} match ${MyUtil.getSmallCamelCase(it)} }" }
                                .joinToString("")
                    }
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
                    }): SqlUpdateClip<${entityTableMetaName}> {
        return this.update()${
                        keys.map { ".where{ it.${it.replace(".", "_")} match ${MyUtil.getSmallCamelCase(it)} }" }
                                .joinToString("")
                    }
    }
"""
            )
        }

        var dbName = entType.getAnnotation(DbName::class.java)?.value ?: ""

        if (dbName.isEmpty()) {
            dbName = tableName
        }


        val ret = EntityResult();
        ret.body = """
class ${entityTableMetaName}(datasource:String="")
    :SqlBaseMetaTable<${entType.name.GetSafeKotlinName()}>(${entType.name.GetSafeKotlinName()}::class.java,"${dbName}") {
${columnMetaDefines.props.joinToString("\n")}

    override fun getSpreadColumns(): Array<String> { return arrayOf<String>(${
            columnMetaDefines.columns_spread.map { "\"" + it + "\"" }.joinToString(",")
        })}

    override fun getAutoIncrementKey(): String { return "${columnMetaDefines.autoIncrementKey}"}
    override fun getUks(): Array<Array<String>>{ return arrayOf(${uks2.joinToString(",")} )}
    override fun getFks(): Array<FkDefine>{ return arrayOf(${fks_exp_string.joinToString(",")})}

${idMethods.joinToString("\n")}
}"""
        //override fun getColumns(): SqlColumnNames { return SqlColumnNames(${columnMetaDefines.columns.joinToString(",")})}
        ret.ext = columnMetaDefines.extMethods.joinToString("\n");
        return ret;
    }

    data class ColumnMetaDefine @JvmOverloads constructor(
            var tableName: String = "",
//            var entityTypeName: String = "",
            var autoIncrementKey: String = "",
            var uks: MutableSet<String> = mutableSetOf(),
            var pks: MutableSet<String> = mutableSetOf(),
//            var columns_convertValue: MutableSet<String> = mutableSetOf(),
            var columns: MutableSet<String> = mutableSetOf(),
            var columns_spread: MutableSet<String> = mutableSetOf(),
            var props: MutableSet<String> = mutableSetOf(),
            var extMethods: MutableSet<String> = mutableSetOf(),
            var fks: MutableSet<FkDefine> = mutableSetOf()
    )

    private fun getColumnMetaDefines(
            //原始的组名
            groupName: String,
            //原始的Meta数据，表名
            entityName: String,
            //可递归的类名
            entType: Class<*>,
            parentEntityPrefixs: Array<String> = arrayOf()
    ): ColumnMetaDefine {
        val ret = ColumnMetaDefine();
        val parentEntityPrefix = parentEntityPrefixs.map { it + "_" }.joinToString("")

        val tableName = parentEntityPrefix + entType.name.split(".").last();
        ret.tableName = tableName
        if (ret.tableName.endsWith("\$Companion")) {
            return ret;
        }


        val fk_define = entType.getAnnotation(SqlFks::class.java)
        if (fk_define != null) {
            fk_define.value.forEach {
                ret.fks.add(
                        FkDefine(
                                tableName,
                                parentEntityPrefix + it.fieldName,
                                parentEntityPrefix + it.refTable,
                                parentEntityPrefix + it.refTableColumn
                        )
                )
            }
        }

//        val entDbType = DbType.of(entType)
//        if (entDbType != DbType.Other) {
//            throw RuntimeException("遇到了简单类型?")
//        }

        entType.AllFields
                .filter { it.name != "Companion" }
                .forEach { field ->
                    field.isAccessible = true
                    var db_column_name = parentEntityPrefix + field.name;

//                    var dbName = field.getAnnotation(DbName::class.java);
//                    if (dbName != null) {
//                        db_column_name = dbName.value;
//                    }

                    if (field.getAnnotation(SqlAutoIncrementKey::class.java) != null) {
                        ret.autoIncrementKey = db_column_name
                        ret.uks.add(db_column_name)
                    }

                    if (field.getAnnotation(DbKey::class.java) != null) {
                        ret.pks.add(db_column_name);
                    }


//                    var converter = field.getAnnotation(ConverterValueToDb::class.java)
//                    var ann_converter = "";
//                    if (converter != null) {
//                        ret.columns_convertValue.add(db_column_name);
//                        ann_converter =
//                                "@ConverterValueToDb(" + (converter.value.map { it.qualifiedName + "::class" }.joinToString(",")
//                                        ?: "") + ")\n";
//                    }

                    val fieldDbType = DbType.of(field.type)
                    if (fieldDbType == DbType.Other) {

                        //看是否是展开列。
                        if (field.type.IsCollectionType || Map::class.java.isAssignableFrom(field.type)) {
                            ret.columns.add(db_column_name);

                            var item =
                                    """val ${parentEntityPrefix + field.name} = SqlColumnName(DbType.Json, this.getAliaTableName(),"${db_column_name}")""".ToTab(
                                            1
                                    )
                            ret.props.add(item);

                            return@forEach
                        } else {
                            ret.columns_spread.add(parentEntityPrefix + field.name)
                            ret.extMethods.add(getExtMethod(groupName, entityName, parentEntityPrefixs + field.name, field.type))

                            var spreadResult = getColumnMetaDefines(groupName, entityName, field.type, parentEntityPrefixs + field.name);

                            ret.uks.addAll(spreadResult.uks)
//                            ret.columns_convertValue.addAll(spreadResult.columns_convertValue)
                            ret.columns.addAll(spreadResult.columns)
                            ret.columns_spread.addAll(spreadResult.columns_spread)
                            ret.props.addAll(spreadResult.props)
                            ret.extMethods.addAll(spreadResult.extMethods)
                            ret.fks.addAll(spreadResult.fks)

                            return@forEach
                        }

                    } else {
                        ret.columns.add(db_column_name);
                    }

                    var item =
                            """val ${parentEntityPrefix + field.name} = SqlColumnName(DbType.${fieldDbType.name}, this.getAliaTableName(),"${db_column_name}")""".ToTab(
                                    1
                            )
                    ret.props.add(item);
                }


        if (ret.pks.any()) {
            ret.uks.add(ret.pks.joinToString(","))
        }


        var uks_define = entType.getAnnotation(DbUks::class.java)
        if (uks_define != null) {
            ret.uks.addAll(uks_define.value)
        }

        return ret;
    }

    private fun getExtMethod(groupName: String, entityName: String, spreadColumnNames: Array<String>, spreadColumnType: Class<*>): String {

        val GroupName = MyUtil.getBigCamelCase(groupName)
        val paramName = spreadColumnNames.joinToString("_")
        val entityTableMetaName = getEntityClassName(entityName)

        return """
fun SqlUpdateClip<${GroupName}Group.${entityTableMetaName}>.set_${MyUtil.getSmallCamelCase(entityName)}_${paramName}(${paramName}:${spreadColumnType.name}):SqlUpdateClip<${GroupName}Group.${entityTableMetaName}>{
    return this${getSpreadFields(spreadColumnNames, spreadColumnType).joinToString("\n\t\t")}
}
"""
    }

    private fun getSpreadFields(spreadColumnNames: Array<String>, spreadColumnType: Class<*>, varLen: Int = spreadColumnNames.size): List<String> {
        var list = mutableListOf<String>()
        spreadColumnType.AllFields
                .forEach {
                    var dbType = DbType.of(it.type)

                    if (dbType != DbType.Other || it.type.IsCollectionType || Map::class.java.isAssignableFrom(it.type)) {
                        var varName = spreadColumnNames.take(varLen).joinToString("_")
                        var subVarNameWithDot = spreadColumnNames.Slice(varLen).map { it + "." }.joinToString("");
                        list.add(".set{ it." + spreadColumnNames.joinToString("_") + "_" + it.name + " to " + varName + "." + subVarNameWithDot + it.name + " }");
                        return@forEach
                    }

                    list.addAll(getSpreadFields(spreadColumnNames + it.name, it.type, varLen));
                }

        return list;
    }
}
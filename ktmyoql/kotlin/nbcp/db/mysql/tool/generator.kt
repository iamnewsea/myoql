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

            writeToFile(
                """
@Component("sql.${group.key}")
@MetaDataGroup("${group.key}")
class ${MyUtil.getBigCamelCase(group.key)}Group : IDataGroup{
    override fun getEntities():Set<BaseMetaData> = setOf(${
                    group.value.map { genVarName(it).GetSafeKotlinName() }.joinToString(",")
                })
"""
            )
            println("${group.key}:")
            group.value.forEach { entityType ->
                count++;
                println("${count.toString().padStart(2, ' ')} 生成实体：${group.key}.${entityType.simpleName}".ToTab(1))
                writeToFile(genVarEntity(entityType).ToTab(1))
            }

            writeToFile("\n")

            group.value.forEach { entityType ->
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
        moer_File.appendln(msg)
        moer_File.flush()
//        println(msg)
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
        var tableName = entType.name.split(".").last();
        if (tableName.endsWith("\$Companion")) {
            return EntityResult();
        }

        var autoIncrementKey = "";
        var uks = mutableSetOf<String>()
//        var rks = mutableSetOf<String>()
        var fks = mutableSetOf<FkDefine>()
        var pks = mutableListOf<String>()
        var columns = mutableListOf<String>()
        var columns_spread = mutableListOf<String>()
        var columns_convertValue = mutableListOf<String>()

        var props = mutableListOf<String>();
        var idMethods = mutableListOf<String>()
        var extMethods = mutableListOf<String>()
        var entityTypeName = getEntityClassName(tableName)

        entType.AllFields
            .filter { it.name != "Companion" }
            .forEach { field ->
                field.isAccessible = true
                var db_column_name = field.name;

//                    var dbName = field.getAnnotation(DbName::class.java);
//                    if (dbName != null) {
//                        db_column_name = dbName.value;
//                    }


                if (field.getAnnotation(SqlAutoIncrementKey::class.java) != null) {
                    autoIncrementKey = db_column_name
                    uks.add(db_column_name)
                }

                if (field.getAnnotation(DbKey::class.java) != null) {
                    pks.add(db_column_name);
                }

                var fk_define = field.getAnnotation(SqlFk::class.java)
                if (fk_define != null) {
                    fks.add(FkDefine(tableName, db_column_name, fk_define.refTable, fk_define.refTableColumn))
                }

                var converter = field.getAnnotation(ConverterValueToDb::class.java)
                var ann_converter = "";
                if (converter != null) {
                    columns_convertValue.add(db_column_name);
                    ann_converter = "@ConverterValueToDb(" + (converter.value.qualifiedName
                        ?: "") + "::class)\n";
                }

                var dbType = DbType.of(field.type)
                if (dbType == DbType.Other) {

                    //看是否是展开列。
//                        var spreadColumn = field.getAnnotation(SqlSpreadColumn::class.java)
//                        if (spreadColumn != null) {
                    if (field.type.IsCollectionType) {
                        throw RuntimeException("TODO 生成关系表，未实现")
                    } else {
                        columns_spread.add(db_column_name);

                        var spread_methods = mutableListOf<String>()
                        var subFields = field.type.AllFields
                            .filter { it.name != "Companion" }

                        spread_methods.add(
                            """
fun SqlUpdateClip<${MyUtil.getBigCamelCase(groupName)}Group.${entityTypeName}>.set_${tableName}_${db_column_name}(${db_column_name}:${field.type.name}):SqlUpdateClip<${
                                MyUtil.getBigCamelCase(
                                    groupName
                                )
                            }Group.${entityTypeName}>{
    return this${
                                subFields.map { ".set{ it." + db_column_name + "_" + it.name + " to " + db_column_name + "." + it.name + " }" }
                                    .joinToString("\n\t\t\t")
                            }
}
"""
                        )

                        subFields
                            .forEach {
                                var db_column_name = db_column_name + "_" + it.name;
                                var dbType = DbType.of(it.type)

                                columns.add(db_column_name);

                                var item =
                                    """val ${field.name}_${it.name} = SqlColumnName(DbType.${dbType.name}, this.getAliaTableName(),"${db_column_name}")""".ToTab(
                                        1
                                    )
                                props.add(item);
                            }



                        extMethods.addAll(spread_methods)

                        return@forEach
                    }
//                        else {
//                            throw RuntimeException("未识别的数据类型,表：${tableName},列:${db_column_name},如果定义复杂列，请在列在添加 @SqlSpreadColumn 注解")
//                        }
                } else {
                    columns.add(db_column_name);
                }

                var item =
                    """${ann_converter}val ${field.name} = SqlColumnName(DbType.${dbType.name}, this.getAliaTableName(),"${db_column_name}")""".ToTab(
                        1
                    )
                props.add(item);
            }



        if (pks.any()) {
            uks.add(pks.joinToString(","))
        }

        kotlin.run {
            var uks_define = entType.getAnnotation(DbUks::class.java)
            if (uks_define != null) {
                uks.addAll(uks_define.value)
            }
        }

//        kotlin.run {
//            var rks_define = entType.getAnnotation(SqlRks::class.java)
//            if (rks_define != null) {
//                rks.addAll(rks_define.rkColumns)
//            }
//        }


        var uks2 = uks.map { """ arrayOf(${it.split(",").map { "\"" + it + "\"" }.joinToString(",")}) """ }
//        var rks2 = rks.map { """ arrayOf(${it.split(",").map { "\"" + it + "\"" }.joinToString(",")}) """ }
        var fks_exp_string =
            fks.map { """FkDefine("${it.table}","${it.column}","${it.refTable}","${it.refColumn}") """ }.toTypedArray()


        uks.forEach { uk ->
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
                }): SqlQueryClip<${entityTypeName}, ${entType.name}> {
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
                }): SqlDeleteClip<${entityTypeName}> {
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
                }): SqlUpdateClip<${entityTypeName}> {
        return this.update()${keys.map { ".where{ it.${it} match ${MyUtil.getSmallCamelCase(it)} }" }.joinToString("")}
    }
"""
            )
        }

        var dbName = entType.getAnnotation(DbName::class.java)?.value ?: ""

        if (dbName.isEmpty()) {
            dbName = tableName
        }


        var ret = EntityResult();
        ret.body = """
class ${entityTypeName}(datasource:String="")
    :SqlBaseMetaTable<${entType.name.GetSafeKotlinName()}>(${entType.name.GetSafeKotlinName()}::class.java,"${dbName}") {
${props.joinToString("\n")}

    override fun getSpreadColumns(): Array<String> { return arrayOf<String>(${
            columns_spread.map { "\"" + it + "\"" }.joinToString(",")
        })}
    override fun getConvertValueColumns(): Array<String> { return arrayOf<String>(${
            columns_convertValue.map { "\"" + it + "\"" }.joinToString(",")
        })}
    override fun getColumns(): SqlColumnNames { return SqlColumnNames(${columns.joinToString(",")})}
    override fun getAutoIncrementKey(): String { return "${autoIncrementKey}"}
    override fun getUks(): Array<Array<String>>{ return arrayOf(${uks2.joinToString(",")} )}
    override fun getFks(): Array<FkDefine>{ return arrayOf(${fks_exp_string.joinToString(",")})}

${idMethods.joinToString("\n")}
}"""
        ret.ext = extMethods.joinToString("\n");

        return ret;
    }
}
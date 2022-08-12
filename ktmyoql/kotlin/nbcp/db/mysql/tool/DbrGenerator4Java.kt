package nbcp.db.mysql.tool

import nbcp.comm.*
import nbcp.data.Sys
import nbcp.utils.*
import nbcp.db.*
import nbcp.db.sql.*
import nbcp.tool.CodeGeneratorHelper
import java.io.File
import java.io.FileWriter
import java.lang.RuntimeException
import java.lang.reflect.ParameterizedType
import java.time.LocalDateTime


private fun _join(vararg args: String): String {
    return args.toList().filter { it.HasValue }.joinToString(".");
}

//private fun <T : SqlColumnName> T.and(vararg value: String): T {
//    if (value.any() == false) return this;
//
//    var con = this.javaClass.getConstructor(String::class.java);
//    return con.newInstance(_join(MyUtil.getValueByWbsPath(this, "_pname").toString(), *value)) as T;
//}

/**
 * Created by udi on 17-6-11.
 * MySql 代码生成器
 */
class DbrGenerator4Java {

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
        targetPath: String, //目标文件
        basePackage: String,    //实体的包名
        packageName:String = "nbcp.db.sql.table",
        packages: Array<String> = arrayOf(),   //import 包名
        entityFilter: ((Class<*>) -> Boolean) = { true },
        nameMapping: StringMap = StringMap(), // 名称转换
        ignoreGroups: List<String> = listOf("MongoBase")  //忽略的包名
    ) {
        targetEntityPathName = MyUtil.joinFilePath(targetPath, packageName.split(".").joinToString("/"))
        this.nameMapping = nameMapping
        var p = File.separator;

//        var path = Thread.currentThread().contextClassLoader.getResource("").path.split("/target/")[0]
//        var moer_Path = File(path).parentFile.path + "/shop-orm/kotlin/nbcp/db/mysql/dbr_tables.kt".replace("/", p);


        File(targetEntityPathName).deleteRecursively();
        File(targetEntityPathName).mkdirs()

        var groups = getGroups(basePackage).filter { ignoreGroups.contains(it.key) == false };


        println("---------------生成 dbr---------------")
        var fileHeader = """package ${packageName};

import nbcp.db.*;
import nbcp.db.sql.*;
import nbcp.db.sql.entity.*;
import nbcp.db.mysql.*;
import nbcp.comm.*;
import nbcp.utils.*;
import java.util.*;
import java.util.stream.*;
import org.springframework.stereotype.*;
${packages.map { "import " + it + ";"}.joinToString(const.line_break)}

//generate auto @${LocalDateTime.now().AsString()}
"""

        var count = 0;


        groups.forEach { group ->
            var exts = mutableListOf<String>();
            var groupEntities = group.value.filter(entityFilter);


            writeToFile("${MyUtil.getBigCamelCase(group.key)}Group",
                fileHeader +
                """
@Component("sql.${group.key}")
@MetaDataGroup(dbType = DatabaseEnum.Sql, value = "${group.key}")
public class ${MyUtil.getBigCamelCase(group.key)}Group implements IDataGroup{
    @Override
    public Set<BaseMetaData> getEntities(){
        return new HashSet(){ { 
${
                    groupEntities
                        .map { "add(" + genVarName(it).GetSafeKotlinName() + ");" }
                        .map { it.ToTab(3) }
                        .joinToString("\n")
                }
        } };
    }
"""
            )
            println("${group.key}:")
            groupEntities
                .forEach { entityType ->
                    count++;
                    println("${count.toString().padStart(2, ' ')} 生成实体：${group.key}.${entityType.simpleName}".ToTab(1))
                    writeToFile("${MyUtil.getBigCamelCase(group.key)}Group", genVarEntity(entityType).ToTab(1))
                }

            writeToFile("${MyUtil.getBigCamelCase(group.key)}Group", "\n")

            groupEntities
                .forEach { entityType ->
                    var item = genEntity(MyUtil.getSmallCamelCase(group.key), entityType)

                    if (item.ext.HasValue) {
                        exts.add(item.ext);
                    }
                    writeToFile("${MyUtil.getBigCamelCase(group.key)}Group", item.body.ToTab(1))
                }

            writeToFile("${MyUtil.getBigCamelCase(group.key)}Group", """}""")

            writeToFile("${MyUtil.getBigCamelCase(group.key)}Group", exts.joinToString("\n"))
        }
    }


    fun writeToFile(className: String, content: String) {
        var moer_File = FileWriter(MyUtil.joinFilePath(targetEntityPathName, className + ".java"), true);
        moer_File.appendLine(content)
        moer_File.flush()
        moer_File.close();
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

    fun getEmbClasses(groups: HashMap<String, MutableList<Class<*>>>): List<Class<*>> {
        return groups.values.Unwind()
                .map {
                    return@map findEmbClasses(it)
                }
                .Unwind()
                .distinctBy { it.name }
                .sortedBy { it.name }
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

        return """public ${getEntityClassName(entTypeName)} ${entityVarName.GetSafeKotlinName()} = new ${getEntityClassName(entTypeName)}();""";
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

        val idMethods = mutableListOf<String>()

        val entityName = entType.name.split(".").last();
        val entityTableMetaName = getEntityClassName(entityName)
        val columnMetaDefines = getColumnMetaDefines(groupName, entityName, entType);


        var uks2 =
            columnMetaDefines.uks.map { """ new String[]{ ${it.split(",").map { "\"" + it + "\"" }.joinToString(",")} } """ }
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
    public SqlQueryClip<${entityTableMetaName}, ${entType.name}> queryBy${
                    keys.map { MyUtil.getBigCamelCase(it) }.joinToString("")
                } (${
                    keys.map {
                        "${
                            entType.GetFieldPath(
                                *it.split(".").toTypedArray()
                            )!!.type.javaTypeName
                        } ${MyUtil.getSmallCamelCase(it)}"
                    }.joinToString(",")
                }) {
        return this.query()${
                    keys.map { ".where(it-> it.${it.replace(".", "_")}.match(${MyUtil.getSmallCamelCase(it)}))" }
                        .joinToString("")
                };
    }

    public SqlDeleteClip<${entityTableMetaName}> deleteBy${
                    keys.map { MyUtil.getBigCamelCase(it) }.joinToString("")
                } (${
                    keys.map {
                        "${
                            entType.GetFieldPath(
                                *it.split(".").toTypedArray()
                            )!!.type.javaTypeName
                        } ${MyUtil.getSmallCamelCase(it)}"
                    }.joinToString(",")
                }) {
        return this.delete()${
                    keys.map { ".where(it-> it.${it.replace(".", "_")}.match(${MyUtil.getSmallCamelCase(it)}))" }
                        .joinToString("")
                };
    }

    public SqlUpdateClip<${entityTableMetaName}> updateBy${
                    keys.map { MyUtil.getBigCamelCase(it) }.joinToString("")
                } (${
                    keys.map {
                        "${
                            entType.GetFieldPath(
                                *it.split(".").toTypedArray()
                            )!!.type.javaTypeName
                        } ${MyUtil.getSmallCamelCase(it)}"
                    }.joinToString(",")
                }) {
        return this.update()${
                    keys.map { ".where(it-> it.${it.replace(".", "_")}.match(${MyUtil.getSmallCamelCase(it)}))" }
                        .joinToString("")
                };
    }
"""
            )
        }

        var dbName = entType.getAnnotation(DbName::class.java)?.value ?: ""

        if (dbName.isEmpty()) {
            dbName = tableName
        }


        val ret = EntityResult();
        ret.body = """${
            CodeGeneratorHelper.getEntityComment(
                entType,
                ""
            )
        }${JavaCoderUtil.getAnnotationCodes(entType.annotations).map { const.line_break + it }.joinToString("")}
public class ${entityTableMetaName} extends SqlBaseMetaTable<${entType.name.GetSafeKotlinName()}> {
    private String collectionName = "";
    private String datasource = "";
    
    public ${entityTableMetaName}(){
        this("","");
    }
    public ${entityTableMetaName}(String collectionName, String datasource){
        super(${entType.name.GetSafeKotlinName()}.class, "${dbName}", MyHelper.AsString(collectionName,"${dbName}"), datasource);
        this.collectionName = collectionName;
        this.datasource = datasource;
    }
    
    
${columnMetaDefines.props.joinToString("\n")}

    @Override
    public String[] getSpreadColumns() {
        return new String[] { ${
            columnMetaDefines.columns_spread.map { "\"" + it + "\"" }.joinToString(",")
        }
        };
    }

    @Override
    public String getAutoIncrementKey() { return "${columnMetaDefines.autoIncrementKey}"; }
    @Override
    public String[][] getUks() { return new String[][]{ ${uks2.joinToString(",")} }; }
    @Override
    public FkDefine[] getFks() { return new FkDefine[]{ ${fks_exp_string.joinToString(",")} }; }

${idMethods.joinToString("\n")}

    public SqlQueryClip<${entityTableMetaName}, ${entType.name}> query(){
        return new SqlQueryClip(this);
    }
    
    public SqlUpdateClip<${entityTableMetaName}> update(){
        return new SqlUpdateClip(this);
    }
    
    public SqlDeleteClip<${entityTableMetaName}> delete(){
        return new SqlDeleteClip(this);
    }
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


        val fk_defines = entType.getAnnotationsByType(SqlFk::class.java)
        fk_defines.forEach {
            ret.fks.add(
                FkDefine(
                    tableName,
                    parentEntityPrefix + it.fieldName,
                    parentEntityPrefix + it.refTable,
                    parentEntityPrefix + it.refTableColumn
                )
            )
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


                if (field.getAnnotation(SqlAutoIncrementKey::class.java) != null) {
                    ret.autoIncrementKey = db_column_name
                    ret.uks.add(db_column_name)
                }


                val fieldDbType = DbType.of(field.type)
                if (fieldDbType == DbType.Other) {

                    //看是否是展开列。
                    if (field.type.IsCollectionType || Map::class.java.isAssignableFrom(field.type)) {
                        ret.columns.add(db_column_name);

                        var item =
                            """public SqlColumnName ${parentEntityPrefix + field.name} = new SqlColumnName(DbType.Json, this.getAliaTableName(),"${db_column_name}");""".ToTab(
                                1
                            )
                        ret.props.add(item);

                        return@forEach
                    } else {
                        ret.columns_spread.add(parentEntityPrefix + field.name)
                        ret.extMethods.add(
                            getExtMethod(
                                groupName,
                                entityName,
                                parentEntityPrefixs + field.name,
                                field.type
                            )
                        )

                        var spreadResult =
                            getColumnMetaDefines(groupName, entityName, field.type, parentEntityPrefixs + field.name);

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
                    """public SqlColumnName ${parentEntityPrefix + field.name} = new SqlColumnName(DbType.${fieldDbType.name}, this.getAliaTableName(),"${db_column_name}");""".ToTab(
                        1
                    )
                ret.props.add(item);
            }


        ret.uks.addAll(CodeGeneratorHelper.getEntityUniqueIndexesDefine(entType))

        return ret;
    }

    private fun getExtMethod(
        groupName: String,
        entityName: String,
        spreadColumnNames: Array<String>,
        spreadColumnType: Class<*>
    ): String {

        val GroupName = MyUtil.getBigCamelCase(groupName)
        val paramName = spreadColumnNames.joinToString("_")
        val entityTableMetaName = getEntityClassName(entityName)

        return """
public SqlUpdateClip<${GroupName}Group.${entityTableMetaName}> SqlUpdateClip<${GroupName}Group.${entityTableMetaName}>.set_${MyUtil.getSmallCamelCase(entityName)}_${paramName}(${paramName}:${spreadColumnType.name}) {
    return this${getSpreadFields(spreadColumnNames, spreadColumnType).joinToString("\n\t\t")}
}
"""
    }

    private fun getSpreadFields(
        spreadColumnNames: Array<String>,
        spreadColumnType: Class<*>,
        varLen: Int = spreadColumnNames.size
    ): List<String> {
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
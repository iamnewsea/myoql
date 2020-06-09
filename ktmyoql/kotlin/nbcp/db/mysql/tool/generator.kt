package nbcp.db.mysql.tool

import nbcp.comm.*
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

    fun work(targetFileName: String, //目标文件
             basePackage: String,    //实体的包名
             anyEntityClass: Class<*>, //任意实体的类名
             nameMapping: StringMap = StringMap(), // 名称转换
             ignoreGroups: List<String> = listOf("MongoBase")  //忽略的包名
    ) {
        this.nameMapping = nameMapping
        var p = System.getProperty("file.separator");

//        var path = Thread.currentThread().contextClassLoader.getResource("").path.split("/target/")[0]
//        var moer_Path = File(path).parentFile.path + "/shop-orm/kotlin/nbcp/db/mysql/dbr_tables.kt".replace("/", p);
        var moer_Path = targetFileName.replace("/", p);

        File(moer_Path).delete();
        File(moer_Path).createNewFile()

        moer_File = FileWriter(moer_Path, true);
        var groups = getGroups(basePackage, anyEntityClass).filter { ignoreGroups.contains(it.key) == false };


        println("---------------生成 dbr---------------")

        writeToFile("""package nbcp.db.sql.table

import nbcp.db.*
import nbcp.db.sql.*
import nbcp.db.sql.entity.*
import nbcp.db.mysql.*
import nbcp.db.mysql.entity.*
import nbcp.comm.*
import nbcp.utils.*
import org.springframework.stereotype.Component

//generate auto @${LocalDateTime.now().AsString()}
""")
        var count = 0;

        groups.forEach { group ->

            writeToFile("""
@Component("sql.${group.key}")
@MetaDataGroup("${group.key}")
class ${MyUtil.getBigCamelCase(group.key)}Group : IDataGroup{
    override fun getEntities():Set<BaseMetaData> = setOf(${group.value.map { genVarName(it) }.joinToString(",")})
""")
            println("${group.key}:")
            group.value.forEach { entityType ->
                count++;
                println("${count.toString().padStart(2, ' ')} 生成实体：${group.key}.${entityType.simpleName}".ToTab(1))
                writeToFile(genVarEntity(entityType).ToTab(1))
            }

            writeToFile("\n")

            group.value.forEach { entityType ->
                writeToFile(genEntity(MyUtil.getSmallCamelCase(group.key), entityType).ToTab(1))
            }

            writeToFile("""}""")

        }

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

        return """val ${entityVarName} get()= ${getEntityClassName(entTypeName)}();""";
    }

    fun genEntity(groupName: String, entType: Class<*>): String {

        var tableName = entType.name.split(".").last();
        if (tableName.endsWith("\$Companion")) {
            return "";
        }

        var autoIncrementKey = "";
        var uks = mutableSetOf<String>()
        var rks = mutableSetOf<String>()
        var fks = mutableSetOf<FkDefine>()

        var pks = mutableListOf<String>()

        var props = entType.AllFields
                .filter { it.name != "Companion" }
                .map {
                    it.isAccessible = true
                    var db_column_name = it.name;

                    if (it.getAnnotation(SqlAutoIncrementKey::class.java) != null) {
                        autoIncrementKey = it.name
                        uks.add(it.name)
                    }

                    if (it.getAnnotation(DbKey::class.java) != null) {
                        pks.add(it.name);
                    }

                    var fk_define = it.getAnnotation(SqlFk::class.java)
                    if (fk_define != null) {
                        fks.add(FkDefine(tableName, db_column_name, fk_define.refTable, fk_define.refTableColumn))
                    }

                    var converter = it.getAnnotation(ConverterValueToDb::class.java)
                    var ann_converter = "";
                    if (converter != null) {
                        ann_converter = "@ConverterValueToDb(" + (converter.converter.qualifiedName
                                ?: "") + "::class)\n";
                    }

                    var dbType = DbType.of(it.type)
                    if (dbType == DbType.Other) {
                        throw RuntimeException("未识别的数据类型,表：${tableName},列:${db_column_name}")
                    }
                    return@map """${ann_converter}val ${it.name}=SqlColumnName(DbType.${dbType.name},this.getAliaTableName(),"${db_column_name}")""".ToTab(1)
                }

        var entityTypeName = getEntityClassName(tableName)


        if (pks.any()) {
            uks.add(pks.joinToString(","))
        }

        kotlin.run {
            var uks_define = entType.getAnnotation(DbUks::class.java)
            if (uks_define != null) {
                uks.addAll(uks_define.ukColumns)
            }
        }

        kotlin.run {
            var rks_define = entType.getAnnotation(SqlRks::class.java)
            if (rks_define != null) {
                rks.addAll(rks_define.rkColumns)
            }
        }


        var uks2 = uks.map { """ arrayOf(${it.split(",").map { "\"" + it + "\"" }.joinToString(",")}) """ }
        var rks2 = rks.map { """ arrayOf(${it.split(",").map { "\"" + it + "\"" }.joinToString(",")}) """ }
        var fks_exp_string = fks.map { """FkDefine("${it.table}","${it.column}","${it.refTable}","${it.refColumn}") """ }.toTypedArray()

        var idMethods = mutableListOf<String>()
        uks.forEach { uk ->
            var keys = uk.split(",")
            //检测
            keys.forEach {
                if (entType.GetFieldPath(*it.split(".").toTypedArray()) == null) {
                    throw RuntimeException("${tableName} 找不到 ${it} 属性!")
                }
            }

            idMethods.add("""
    fun queryBy${keys.map { MyUtil.getBigCamelCase(it) }.joinToString("")} (${keys.map { "${MyUtil.getSmallCamelCase(it)}: ${entType.GetFieldPath(*it.split(".").toTypedArray())!!.type.kotlinTypeName}" }.joinToString(",")}): SqlQueryClip<${entityTypeName}, ${entType.name}> {
        return this.query()${keys.map { ".where{ it.${it} match ${MyUtil.getSmallCamelCase(it)} }" }.joinToString("")}
    }

    fun deleteBy${keys.map { MyUtil.getBigCamelCase(it) }.joinToString("")} (${keys.map { "${MyUtil.getSmallCamelCase(it)}: ${entType.GetFieldPath(*it.split(".").toTypedArray())!!.type.kotlinTypeName}" }.joinToString(",")}): SqlDeleteClip<${entityTypeName},${entType.name}> {
        return this.delete()${keys.map { ".where{ it.${it} match ${MyUtil.getSmallCamelCase(it)} }" }.joinToString("")}
    }

    fun updateBy${keys.map { MyUtil.getBigCamelCase(it) }.joinToString("")} (${keys.map { "${MyUtil.getSmallCamelCase(it)}: ${entType.GetFieldPath(*it.split(".").toTypedArray())!!.type.kotlinTypeName}" }.joinToString(",")}): SqlUpdateClip<${entityTypeName},${entType.name}> {
        return this.update()${keys.map { ".where{ it.${it} match ${MyUtil.getSmallCamelCase(it)} }" }.joinToString("")}
    }
""")
        }

        var dbName = entType.getAnnotation(DbName::class.java)?.value ?: ""

        if (dbName.isEmpty()) {
            dbName = tableName
        }


        return """
class ${entityTypeName}(datasource:String="")
    :SqlBaseMetaTable<${entType.name}>(${entType.name}::class.java,"${dbName}") {
${props.joinToString("\n")}

    override fun getAutoIncrementKey(): String { return "${autoIncrementKey}"}
    override fun getUks(): Array<Array<String>>{ return arrayOf(${uks2.joinToString(",")} )}
    override fun getRks(): Array<Array<String>>{ return arrayOf(${rks2.joinToString(",")} )}
    override fun getFks(): Array<FkDefine>{ return arrayOf(${fks_exp_string.joinToString(",")})}

${idMethods.joinToString("\n")}
}"""
    }
}
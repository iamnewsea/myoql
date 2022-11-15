package nbcp.myoql.db.mysql.tool

import nbcp.base.comm.*
import nbcp.base.db.*
import nbcp.base.enums.*
import nbcp.base.extend.*
import nbcp.base.utils.*
import nbcp.myoql.db.*
import nbcp.myoql.db.comm.*
import nbcp.myoql.db.enums.*
import nbcp.myoql.db.sql.annotation.*
import nbcp.myoql.db.sql.base.*
import nbcp.myoql.db.sql.component.RawQuerySqlClip
import nbcp.myoql.db.sql.enums.DbType
import nbcp.myoql.tool.FreemarkerUtil
import java.lang.reflect.Field
import java.time.LocalDateTime
import java.util.*
import javax.sql.DataSource


/**
 * MySql 实体生成器
 */
object MysqlEntityGenerator {

    @JvmStatic
    fun db2Entity() = DbEntityBuilder();


    /**
     * 生成数据库表的实体代码。
     * @param db: 数据库名
     */
    class DbEntityBuilder() {

        private var tableCallback: ((String) -> Boolean)? = null

        fun whereTable(tableCallback: ((String) -> Boolean)?): DbEntityBuilder {
            this.tableCallback = tableCallback;
            return this;
        }

        /**
         * TODO: 需要好好整理一下。
         */
        fun toMarkdown(): List<IdName> {
            var ret = mutableListOf<IdName>()
            var data = getTablesData();

            //先对 group分组
            data.groupBy { it.group }
                .forEach {
                    var group = it.key
                    var entitys = it.value

                    var map = JsonMap(
                        "entitys" to entitys
                    )

                    var code = FreemarkerUtil.process("/markdown-template/mysql_markdown.ftl", map);
                    ret.add(IdName(group, code));
                }

            return ret;
        }

        /**
         * 生成数据库表的实体代码。
         */
        fun toKotlinCode(): List<IdName> {
            var ret = mutableListOf<IdName>()
            var data = getTablesData();

            //先对 group分组
            data.groupBy { it.group }
                .forEach {
                    var group = it.key
                    var entitys = it.value

                    var map = JsonMap(
                        "entitys" to entitys
                    )

                    var code = FreemarkerUtil.process("/myoql-template/mysql/mysql_myoql_entity.ftl", map);
                    ret.add(IdName(group, code));
                }

            return ret;
        }

        fun toJpaCode(packageName: String, vararg baseEntityClass: Class<*>): List<IdName> {
            var ret = mutableListOf<IdName>()
            var data = getTablesData();

            //先对 group分组
            data.forEach {
                var entity = it
//                var map = JsonMap(
//                        "package" to packageName,
//                        "package_base" to packageName.split(".").take(2).joinToString("."),
//                        "entity" to entity,
//                        "field_name" to field_name()
//                )

                var entInfo = BaseEntityInfo(entity, baseEntityClass)
                var code = """package ${packageName};
import ${packageName.split(".").take(2).joinToString(".")}.*;
import lombok.*;
import java.time.*;
import java.util.*;
import nbcp.myoql.db.*;
import java.lang.*;

/**
* Created by CodeGenerator at ${LocalDateTime.now().AsString()}
*/
@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Entity(name = "${entity.name}")
@DbName(name = "${entity.name}")
@Cn("${entity.comment}")
public class ${entity.className} ${entInfo.getBaseClasseString()} ${entInfo.getBaseInterfaceString()} {
${
                    entInfo.getJpaStyleFields().joinToString("\n")
                        .ToTab(1)
                }
}
"""

                ret.add(IdName(entity.name, code));
            }

            return ret;
        }

        fun getTablesData(): List<EntityDbItemData> {
            var ret = mutableListOf<EntityDbItemData>()

            var db = SpringUtil.getBean<DataSource>().connection.use {
                return@use it.catalog;
            }

            var tables_map = RawQuerySqlClip(
                """
SELECT table_name,table_comment
FROM INFORMATION_SCHEMA.TABLES
where table_schema = {db} 
order by table_name
""", JsonMap("db" to db)
            ).toMapList()


            var columns_map = RawQuerySqlClip(
                """
SELECT table_name , column_name , data_type , column_type, column_comment, column_key,extra
FROM INFORMATION_SCHEMA.COLUMNS
where table_schema = {db}  
order by table_name , ordinal_position
""", JsonMap("db" to db)
            ).toMapList()


            var indexes_map = RawQuerySqlClip(
                """
SELECT table_name ,index_name,seq_in_index,column_name 
FROM INFORMATION_SCHEMA.STATISTICS
where table_schema = {db} AND non_unique = 0 AND INDEX_name != 'PRIMARY' 
ORDER BY TABLE_NAME , index_name , seq_in_index
""", JsonMap("db" to db)
            ).toMapList()



            tables_map.filter { tableMap ->
                if (tableCallback != null) {
                    return@filter tableCallback!!(tableMap.getStringValue("table_name")!!)
                }
                return@filter true;
            }
                .forEach { tableMap ->
                    var tableData = EntityDbItemData()

                    tableData.name = tableMap.getStringValue("table_name")!!;
                    tableData.commentString = tableMap.getStringValue("table_comment").AsString()
                        .replace("\r\n", " ")
                        .replace('\n', ' ')
                        .replace('\"', '＂')
                        .replace('\$', '＄')
                        .replace('#', '＃')


                    columns_map.filter { it.getStringValue("table_name") == tableData.name }
                        .forEach colMap@{ columnMap ->

                            var columnName = columnMap.getStringValue("column_name")!!
                            var dataType = columnMap.getStringValue("data_type").AsString()
                            var columnComment = columnMap.getStringValue("column_comment").AsString()
                                .replace("\r\n", " ")
                                .replace('\n', ' ')
                                .replace('\"', '＂')
                                .replace('\$', '＄')
                                .replace('#', '＃')

                            var dbType = DbType.String
                            var remark = "";

                            if (dataType basicSame "varchar"
                                || dataType basicSame "char"
                                || dataType basicSame "nvarchar"
                                || dataType basicSame "nchar"
                            ) {
                                dbType = DbType.String
                            } else if (dataType basicSame "text"
                                || dataType basicSame "mediumtext"
                                || dataType basicSame "longtext"
                            ) {
                                dbType = DbType.Text
                            } else if (dataType basicSame "enum") {
                                dbType = DbType.Enum
                            } else if (dataType basicSame "json") {
                                dbType = DbType.Json
                            } else if (dataType basicSame "int") {
                                dbType = DbType.Int
                            } else if (dataType basicSame "bit") {
                                dbType = DbType.Boolean
                            } else if (dataType basicSame "datetime" ||
                                dataType basicSame "timestamp"
                            ) {
                                dbType = DbType.DateTime
                            } else if (dataType basicSame "date") {
                                dbType = DbType.Date
                            } else if (dataType basicSame "float") {
                                dbType = DbType.Float
                            } else if (dataType basicSame "double") {
                                dbType = DbType.Double
                            } else if (dataType basicSame "long") {
                                dbType = DbType.Long
                            } else if (dataType basicSame "tinyint") {
                                dbType = DbType.Byte
                            } else if (dataType basicSame "bigint") {
                                dbType = DbType.Long
                            } else if (dataType basicSame "decimal") {
                                remark = "warning sql data type: ${dataType}";
                                dbType = DbType.Double
                            }

                            var columnData = EntityDbItemFieldData()
                            columnData.name = columnName
                            columnData.commentString = columnComment
                            columnData.sqlType = columnMap.getStringValue("column_type") ?: ""
                            columnData.dbType = dbType

                            if (columnMap.getStringValue("extra") == "auto_increment") {
                                columnData.autoInc = true
                            }

                            if (remark.HasValue) {
                                columnData.remark = remark
                            }

                            tableData.columns.add(columnData)
                        }

                    var uks = mutableListOf<String>();

                    uks.add(columns_map.filter {
                        it.getStringValue("table_name") == tableData.name && it.getStringValue(
                            "column_key"
                        ) == "PRI"
                    }
                        .map { it.getStringValue("column_name") }
//                    .map { """"${it}""" }
                        .joinToString(",")
                    )

                    indexes_map.filter { it.getStringValue("table_name") == tableData.name }
                        .groupBy { it.getStringValue("index_name") }
                        .forEach {
                            uks.add(it.value.map { it.getStringValue("column_name") }
//                            .map { """"${it}"""" }
                                .joinToString(",")
                            )
                        }

                    tableData.uks = uks
                        .map { """"${it}"""" }
                        .toTypedArray()

                    ret.add(tableData)
                }

            return ret;
        }
    }

    private fun getVarcharLen(field: Field): Int {
        var len = field.getAnnotation(DataLength::class.java);
        if (len != null) {
            return len.value
        }

        if (field.name.contains("id")) return 48;
        if (field.name.contains("code")) return 48;
        if (field.name.contains("name")) return 64;
        if (field.name.contains("data")) return 256;
        return 64
    }

    private fun getEnumItems(column: Class<*>): String {
        if (column.isEnum == false) return ""

        return column.enumConstants.map { it.toString() }.joinToString(",")
    }

    fun getColumnDefine(
        property: Field,
        nameType: NameMappingTypeEnum = NameMappingTypeEnum.Origin,
        pFieldName: String = "",
        pCn: String = ""
    ): Pair<List<String>, List<String>> {
        var list = mutableListOf<String>()
        var checks = mutableListOf<String>()
        var columnName = pFieldName + nameType.getResult(property.name);
        var propertyType = property.type as Class<*>

        var dbType = DbType.of(propertyType);
        var type = property.getAnnotation(SqlColumnType::class.java)?.value
            .AsString {
                dbType.toMySqlTypeString(getVarcharLen(property), getEnumItems(propertyType))
            }

        var comment = arrayOf(pCn, property.getAnnotation(Cn::class.java)?.value.AsString()).filter { it.HasValue }
            .joinToString(" ")
        var spreadColumn = property.getAnnotation(SqlSpreadColumn::class.java);
        if (spreadColumn != null) {
            propertyType.AllFields.forEach {
                getColumnDefine(it, nameType, columnName + spreadColumn.value, comment)
                    .apply {
                        list.addAll(this.first)
                        checks.addAll(this.second)
                    }
//                var columnNameValue = columnName + spreadColumn.value + it.name;
//
//                var sqlTypeString = it.getAnnotation(SqlColumnType::class.java)?.value
//                    .AsString {
//                        DbType.of(it.type)
//                            .toMySqlTypeString(getVarcharLen(it), getEnumItems(propertyType))
//                    }
//
//
//                var comment = it.getAnnotation(Cn::class.java)?.value.AsString()
//
//
//                var item =
//                    """`${columnNameValue}` ${sqlTypeString} not null ${if (it.type.IsNumberType) "default '0'" else if (propertyType.IsStringType) "default ''" else ""} comment '${comment}'"""
//                list.add(item);
            }

            return list to checks;
        } else if (dbType == DbType.Json || dbType == DbType.Other) {
            //生成关系表
            if (propertyType.IsCollectionType) {
                var item =
                    """`${columnName}` Json not null  default '[]' comment '${comment}'"""
                list.add(item);

                checks.add("CONSTRAINT `c_${columnName}` CHECK ( json_valid(`${columnName}`) )")
            } else {
                var item =
                    """`${columnName}` Json not null  default '{}' comment '${comment}'"""
                list.add(item);

                checks.add("CONSTRAINT `c_${columnName}` CHECK ( json_valid(`${columnName}`) )")
            }
        } else {
            var item =
                """`${columnName}` ${type} not null ${if (propertyType.IsNumberType) "default '0'" else if (propertyType.IsStringType) "default ''" else ""} comment '${comment}'"""
            list.add(item);
        }

        return list to checks;
    }

    /**
     * 生成实体的 sql 代码
     */
    @JvmStatic
    fun entity2Sql(entity: Class<*>, nameType: NameMappingTypeEnum = NameMappingTypeEnum.Origin): String {
        var list = mutableListOf<String>();

        var fields = entity.AllFields
            .sortedBy {
                // id,code,name 这三个字段提前。
                if (it.name basicSame "id") return@sortedBy -9;
                if (it.name basicSame "code") return@sortedBy -8;
                if (it.name basicSame "name") return@sortedBy -7;


                // 其它系统字段最后
                if (it.name basicSame "remark") return@sortedBy 1000 + it.name.length;
                if (it.name.contains("delete", true)) return@sortedBy 1000 + it.name.length;
                if (it.name.contains("create", true)) return@sortedBy 1000 + it.name.length;
                if (it.name.contains("update", true)) return@sortedBy 1000 + it.name.length;
                return@sortedBy it.name.length;
            }

        var checks = mutableListOf<String>();

        fields.forEach {
            getColumnDefine(it, nameType)
                .apply {
                    list.addAll(this.first);
                    checks.addAll(this.second);
                }
        }

        var tableName = nameType.getResult(entity.simpleName)

        return """
DROP TABLE IF EXISTS `${tableName}`;
CREATE TABLE IF NOT EXISTS `${tableName}` (
${list.joinToString(const.line_break + ",")}
, PRIMARY KEY ( ${getPk(entity, nameType).map { "`${it}`" }.joinToString(", ").AsString("!没有主键!")} )
${checks.map { ", " + it }.joinToString("\n")}
) ENGINE=InnoDB  COMMENT='${entity.getAnnotation(Cn::class.java)?.value.AsString()}';
"""
    }

    /**
     * 从众多唯一索引中确定唯一索引。
     */
    fun getPk(entity: Class<*>, nameType: NameMappingTypeEnum = NameMappingTypeEnum.Origin): Set<String> {
        val id = entity.AllFields.firstOrNull { field ->
            var auto = field.getAnnotation(SqlAutoIncrementKey::class.java);
            if (auto != null) {
                return@firstOrNull true
            }
            return@firstOrNull false
        }

        if (id != null) {
            return setOf(nameType.getResult(id.name))
        }

        val indexes = entity.getAnnotation(DbEntityIndexes::class.java)
        if (indexes != null) {
            var ids = indexes.value.filter { it.unique };
            if (ids.any()) {
                return getPk(ids, nameType);
            }
        }

        val index = entity.getAnnotationsByType(DbEntityIndex::class.java)
        if (index != null && index.any()) {
            var ids = index.filter { it.unique }
            return getPk(ids, nameType)
        }

        return setOf()
    }

    private fun getPk(
        ids: List<DbEntityIndex>,
        nameType: NameMappingTypeEnum = NameMappingTypeEnum.Origin
    ): Set<String> {
        return ids
            .sortedBy { it.value.size * 1000 + it.value.map { it.length }.count() }
            .first()
            .value
            .map { nameType.getResult(it) }
            .toSet()
    }

//    class field_name : TemplateMethodModelEx {
//        override fun exec(p0: MutableList<Any?>?): String {
//            var p1 = p0?.elementAt(0).AsString();
//            if (p1.isEmpty()) return "";
//
//            if (MyUtil.allCharIsSameCase(p1)) return p1.lowercase();
//
//            return MyUtil.getSmallCamelCase(p1);
//        }
//    }
}

package nbcp.db.mysql.tool

import nbcp.comm.*
import nbcp.db.sql.*
import nbcp.tool.FreemarkerUtil
import java.time.LocalDateTime
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

/**
 * MySql 实体生成器
 */
object MysqlEntityGenerator {
    fun db2Entity(db: String) = DbEntityBuilder(db);


    /**
     * 生成数据库表的实体代码。
     */
    class DbEntityBuilder(var db: String) {
        private var tableLike = "";
        private var tables = listOf<String>()
        private var excludes = listOf<String>()

        fun tableLike(tableLike: String = ""): DbEntityBuilder {
            this.tableLike = tableLike
            return this;
        }

        fun tables(vararg tables: String): DbEntityBuilder {
            this.tables = tables.toList()
            return this
        }

        fun excludes(vararg excludes: String): DbEntityBuilder {
            this.excludes = excludes.toList()
            return this
        }

        /**
         * 生成数据库表的实体代码。
         */
        fun toKotlinCode(): List<String> {
            var ret = mutableListOf<String>()
            var data = getDbData();

            //先对 group分组
            data.groupBy { it.group }.forEach {
                var group = it.key
                var entitys = it.value

                var map = JsonMap(
                    "entitys" to it.value
                )
                ret.add(FreemarkerUtil.process("myoql_mysql_entity.ftl", map))
            }

            return ret;
        }


        class EntityDbItemFieldData {
            var remark = ""
            var default_value = ""
            var name = ""
            var db_type = ""
            var comment = ""
            var kotlin_type = ""

            var auto_id: Boolean = false
                get() {
                    return comment.contains(Regex("\bauto_id\b", RegexOption.IGNORE_CASE))
                }

            var auto_number: Boolean = false
                get() {
                    return comment.contains(Regex("\bauto_number\b", RegexOption.IGNORE_CASE))
                }

            /*下面四个属性表示该表的单键主键 或 唯一键*/
            var auto_inc: Boolean = false
        }

        class EntityDbItemData {
            var table_name = ""
            var table_comment = ""
            val group: String
                get() {
                    var groups_all_value = Regex("""\(\s*([\w-_]+)\s*\)""")
                        .find(
                            table_comment
                                .replace("（", "(")
                                .replace("）", "")
                        )
                        ?.groupValues ?: listOf()
                    var group_value = ""
                    if (groups_all_value.size > 0) {
                        group_value = groups_all_value[1];
                    }

                    var groups_value = group_value.split(",").map { it.trim() }.filter { it.HasValue }

                    return groups_value.firstOrNull() ?: ""
                }

            var uks = arrayOf<String>()

            var columns = mutableListOf<EntityDbItemFieldData>()
        }

        fun getDbData(): List<EntityDbItemData> {
            var ret = mutableListOf<EntityDbItemData>()

            var tables_map = RawQuerySqlClip(
                SingleSqlData(
                    """
SELECT table_name,table_comment
FROM INFORMATION_SCHEMA.TABLES
where table_schema = {db} 
${if (tableLike.HasValue) " and table_name like '${tableLike}'" else ""}  
${if (tables.any()) " and table_name in (${tables.map { "'" + it + "'" }.joinToString(",")})" else ""}
${if (excludes.any()) " and table_name not in (${excludes.map { "'" + it + "'" }.joinToString(",")})" else ""}
order by table_name
""", JsonMap("db" to db)
                ), "TABLES"
            ).toMapList()

            var columns_map = RawQuerySqlClip(
                SingleSqlData(
                    """
SELECT table_name , column_name , data_type , column_comment, column_key,extra
FROM INFORMATION_SCHEMA.COLUMNS
where table_schema = {db}  
${if (tableLike.HasValue) " and table_name like '${tableLike}'" else ""}  
${if (tables.any()) " and table_name in (${tables.map { "'" + it + "'" }.joinToString(",")})" else ""}
${if (excludes.any()) " and table_name not in (${excludes.map { "'" + it + "'" }.joinToString(",")})" else ""}
order by table_name , ordinal_position
""", JsonMap("db" to db)
                ), "COLUMNS"
            ).toMapList()

            var indexes_map = RawQuerySqlClip(
                SingleSqlData(
                    """
SELECT table_name ,index_name,seq_in_index,column_name 
FROM INFORMATION_SCHEMA.STATISTICS
where table_schema = {db} AND non_unique = 0 AND INDEX_name != 'PRIMARY' 
${if (tableLike.HasValue) " and table_name like '${tableLike}'" else ""}  
${if (tables.any()) " and table_name in (${tables.map { "'" + it + "'" }.joinToString(",")})" else ""}
${if (excludes.any()) " and table_name not in (${excludes.map { "'" + it + "'" }.joinToString(",")})" else ""}
ORDER BY TABLE_NAME , index_name , seq_in_index
""", JsonMap("db" to db)
                ), "COLUMNS"
            ).toMapList()

            tables_map.forEach { tableMap ->
                var tableData = EntityDbItemData()

                tableData.table_name = tableMap.getStringValue("table_name")!!;
                tableData.table_comment = tableMap.getStringValue("table_comment").AsString()

                columns_map.filter { it.getStringValue("table_name") == tableData.table_name }
                    .forEach colMap@{ columnMap ->

                        var columnName = columnMap.getStringValue("column_name")!!
                        var dataType = columnMap.getStringValue("data_type").AsString()
                        var columnComment = columnMap.getStringValue("column_comment").AsString()

                        var kotlinType = dataType
                        var defaultValue = "";
                        var remark = "";

                        if (dataType VbSame "varchar"
                            || dataType VbSame "char"
                            || dataType VbSame "text"
                            || dataType VbSame "mediumtext"
                            || dataType VbSame "longtext"
                            || dataType VbSame "enum"
                        ) {

                            if (dataType VbSame "mediumtext" || dataType VbSame "longtext") {
                                remark = "warning sql data type: ${dataType}";
                            }

                            kotlinType = "String";
                            defaultValue = "\"\"";
                        } else if (dataType VbSame "int") {
                            kotlinType = "Int";
                            defaultValue = "0";
                        } else if (dataType VbSame "bit") {
                            kotlinType = "Boolean?";
                            defaultValue = "null";
                        } else if (dataType VbSame "datetime" ||
                            dataType VbSame "timestamp"
                        ) {
                            kotlinType = "LocalDateTime?"
                            defaultValue = "null"
                        } else if (dataType VbSame "date") {
                            kotlinType = "LocalDate?"
                            defaultValue = "null"
                        } else if (dataType VbSame "float") {
                            kotlinType = "Float"
                            defaultValue = "0F"
                        } else if (dataType VbSame "double") {
                            kotlinType = "Double"
                            defaultValue = "0.0"
                        } else if (dataType VbSame "long") {
                            kotlinType = "Long"
                            defaultValue = "0L"
                        } else if (dataType VbSame "tinyint") {
                            kotlinType = "Byte"
                            defaultValue = "0"
                        } else if (dataType VbSame "bigint") {
                            kotlinType = "Long"
                            defaultValue = "0L"
                        } else if (dataType VbSame "decimal") {
                            remark = "warning sql data type: ${dataType}";
                            kotlinType = "BigDecimal"
                            defaultValue = "BigDecimal.ZERO"
                        }

                        var columnData = EntityDbItemFieldData()
                        columnData.name = columnName
                        columnData.comment = columnComment
                        columnData.db_type = dataType
                        columnData.kotlin_type = kotlinType
                        columnData.default_value = defaultValue


                        if (columnMap.getStringValue("extra") == "auto_increment") {
                            columnData.auto_inc = true
                        }

                        if (remark.HasValue) {
                            columnData.remark = remark
                        }


                        tableData.columns.add(columnData)
                    }

                var uks = mutableListOf<String>();

                uks.add(columns_map.filter {
                    it.getStringValue("table_name") == tableData.table_name && it.getStringValue(
                        "column_key"
                    ) == "PRI"
                }
                    .map { it.getStringValue("column_name") }
                    .map { """"${it}"""" }
                    .joinToString(",")
                )

                indexes_map.filter { it.getStringValue("table_name") == tableData.table_name }
                    .groupBy { it.getStringValue("index_name") }
                    .forEach {
                        uks.add(it.value.map { it.getStringValue("column_name") }
                            .map { """"${it}"""" }
                            .joinToString(",")
                        )
                    }

                tableData.uks = uks.map { """"${it}"""" }.toTypedArray()

                ret.add(tableData)
            }

            return ret;
        }
    }


    /**
     * 生成实体的 sql 代码
     */
    fun entity2Sql(entity: Class<*>): String {
        var list = mutableListOf<String>();

        entity.AllFields
            .sortedBy {
                if (it.name VbSame "id") return@sortedBy -9;
                if (it.name VbSame "name") return@sortedBy -8;
                if (it.name VbSame "code") return@sortedBy -7;
                return@sortedBy it.name.length;
            }
            .forEach { property ->
                var columnName = property.name;
                var propertyType = property.type as Class<*>
                var dbType = DbType.of(propertyType)
                var type = dbType.toMySqlTypeString()

                if (type.isEmpty()) {
                    var spreadColumn = property.getAnnotation(SqlSpreadColumn::class.java)
                    if (spreadColumn != null) {
                        propertyType.AllFields.forEach {
                            var columnName = columnName + "_" + it.name;
                            var dbType = DbType.of(it.type);
                            var type = dbType.toMySqlTypeString()

                            var item =
                                """`${columnName}` ${type} not null ${if (dbType.isNumberic()) "default '0'" else if (propertyType.IsStringType) "default ''" else ""} comment ''"""
                            list.add(item);
                        }

                        return@forEach
                    }
                }

                var item =
                    """`${columnName}` ${type} not null ${if (propertyType.IsNumberType) "default '0'" else if (propertyType.IsStringType) "default ''" else ""} comment ''"""
                list.add(item);
            }

        return """
DROP TABLE IF EXISTS `${entity.simpleName}`;
CREATE TABLE IF NOT EXISTS `${entity.simpleName}` (
${list.joinToString(line_break + ",")}
,PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 COMMENT='';
"""
    }
}
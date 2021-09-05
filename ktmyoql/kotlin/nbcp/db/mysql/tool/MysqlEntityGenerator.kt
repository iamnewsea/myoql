package nbcp.db.mysql.tool

import freemarker.template.TemplateMethodModelEx
import nbcp.comm.*
import nbcp.db.IdName
import nbcp.db.sql.*
import nbcp.tool.FreemarkerUtil
import nbcp.utils.MyUtil
import nbcp.utils.SpringUtil
import java.time.LocalDateTime
import java.util.*
import javax.sql.DataSource


/**
 * MySql 实体生成器
 */
object MysqlEntityGenerator {

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

                    var code = FreemarkerUtil.process("mysql_markdown.ftl", map);
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

                    var code = FreemarkerUtil.process("mysql_myoql_entity.ftl", map);
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
                var map = JsonMap(
                    "package" to packageName,
                    "package_base" to packageName.split(".").take(2).joinToString("."),
                    "entity" to entity,
                    "field_name" to field_name()
                )

                var entInfo = BaseEntityInfo(entity, baseEntityClass)
                var code = """package ${packageName};
import ${packageName.split(".").take(2).joinToString(".")}.*;
import lombok.*;
import java.time.*;
import java.util.*;
import nbcp.db.*;
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
                .filter {
                    if (tableCallback != null) {
                        return@filter tableCallback!!(it.getStringValue("table_name")!!)
                    }
                    return@filter true;
                }

            var columns_map = RawQuerySqlClip(
                """
SELECT table_name , column_name , data_type , column_type, column_comment, column_key,extra
FROM INFORMATION_SCHEMA.COLUMNS
where table_schema = {db}  
order by table_name , ordinal_position
""", JsonMap("db" to db)
            ).toMapList()
                .filter {
                    if (tableCallback != null) {
                        return@filter tableCallback!!(it.getStringValue("table_name")!!)
                    }
                    return@filter true;
                }

            var indexes_map = RawQuerySqlClip(
                """
SELECT table_name ,index_name,seq_in_index,column_name 
FROM INFORMATION_SCHEMA.STATISTICS
where table_schema = {db} AND non_unique = 0 AND INDEX_name != 'PRIMARY' 
ORDER BY TABLE_NAME , index_name , seq_in_index
""", JsonMap("db" to db)
            ).toMapList()
                .filter {
                    if (tableCallback != null) {
                        return@filter tableCallback!!(it.getStringValue("table_name")!!)
                    }
                    return@filter true;
                }


            tables_map.forEach { tableMap ->
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

                        if (dataType VbSame "varchar"
                            || dataType VbSame "char"
                            || dataType VbSame "nvarchar"
                            || dataType VbSame "nchar"
                        ) {
                            dbType = DbType.String
                        } else if (dataType VbSame "text"
                            || dataType VbSame "mediumtext"
                            || dataType VbSame "longtext"
                        ) {
                            dbType = DbType.Text
                        } else if (dataType VbSame "enum") {
                            dbType = DbType.Enum
                        } else if (dataType VbSame "int") {
                            dbType = DbType.Int
                        } else if (dataType VbSame "bit") {
                            dbType = DbType.Boolean
                        } else if (dataType VbSame "datetime" ||
                            dataType VbSame "timestamp"
                        ) {
                            dbType = DbType.DateTime
                        } else if (dataType VbSame "date") {
                            dbType = DbType.Date
                        } else if (dataType VbSame "float") {
                            dbType = DbType.Float
                        } else if (dataType VbSame "double") {
                            dbType = DbType.Double
                        } else if (dataType VbSame "long") {
                            dbType = DbType.Long
                        } else if (dataType VbSame "tinyint") {
                            dbType = DbType.Byte
                        } else if (dataType VbSame "bigint") {
                            dbType = DbType.Long
                        } else if (dataType VbSame "decimal") {
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
//                    .map { """"${it}"""" }
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


    class field_name : TemplateMethodModelEx {
        override fun exec(p0: MutableList<Any?>?): String {
            var p1 = p0?.elementAt(0).AsString();
            if (p1.isEmpty()) return "";

            if (MyUtil.allCharIsSameCase(p1)) return p1.toLowerCase();

            return MyUtil.getSmallCamelCase(p1);
        }

    }
}

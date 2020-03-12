//package nbcp.db.mysql.tool
//
//import nbcp.base.extend.*
//import nbcp.base.line_break
//import nbcp.comm.*
//import nbcp.db.sql.*
//import java.time.LocalDateTime
//import java.util.*
//import kotlin.reflect.KClass
//import kotlin.reflect.full.memberProperties
//import kotlin.reflect.jvm.javaField
//
//class entity_generator{
//    fun db2Entitys(db:String){
//
//        var tables_map = RawQuerySqlClip(SingleSqlData("""
//SELECT table_name,table_comment
//FROM INFORMATION_SCHEMA.TABLES
//where table_schema = {db}
//order by table_name
//""", JsonMap("db" to db)),"TABLES").toMapList()
//
//        var list = RawQuerySqlClip(SingleSqlData("""
//SELECT table_name , column_name , data_type , column_comment
//FROM INFORMATION_SCHEMA.COLUMNS
//where table_schema = {db}
//order by table_name , ordinal_position
//""", JsonMap("db" to db)),"COLUMNS").toMapList()
//
//
//        tables_map.forEach {
//            var tableName = it.getStringValue("table_name");
//            var tableComment = it.getStringValue("table_comment")
//
//            var columns = list.filter { it.getStringValue("table_name") == tableName }
//                    .map {
//                        var columnName = it.getStringValue("column_name")
//                        var dataType = it.getStringValue("data_type")
//                        var columnComment = it.getStringValue("column_comment")
//
//                        var kotlinType = "dataType"
//                        var defaultValue = "(dataType)";
//
//                        if (dataType VbSame "varchar") {
//                            kotlinType = "String"
//                            defaultValue = "\"\"";
//                        } else if (dataType VbSame "int") {
//                            kotlinType = "Int";
//                            defaultValue = "0";
//                        } else if (dataType VbSame "bit") {
//                            kotlinType = "Boolean";
//                            defaultValue = "false";
//                        } else if (dataType VbSame "datetime") {
//                            kotlinType = "LocalDateTime?"
//                            defaultValue = "null"
//                        } else if (dataType VbSame "date") {
//                            kotlinType = "LocalDate?"
//                            defaultValue = "null"
//                        } else if (dataType VbSame "float") {
//                            kotlinType = "Float"
//                            defaultValue = "0F"
//                        } else if (dataType VbSame "double") {
//                            kotlinType = "Double"
//                            defaultValue = "0"
//                        } else if (dataType VbSame "long") {
//                            kotlinType = "Long"
//                            defaultValue = "0L"
//                        }
//
//                        if (columnComment.HasValue) {
//                            """
////${columnComment}
//var ${columnName}: ${kotlinType} = ${defaultValue}"""
//                        } else {
//                            """var ${columnName}: ${kotlinType} = ${defaultValue}"""
//                        }
//                    }
//
//            println("""
//
////${tableComment}
//data class ${tableName}(
//    ${columns.joinToString(",\n").replace("\n", "\n\t")}
//): IBaseDbEntity()
//
//""")
//        }
//    }
//
//    fun entity2Sql(entity: KClass<*>){
//        var list = entity.memberProperties.map { property->
//            var propertyType = property.javaField!!.type as  Class<*>
//            var type = propertyType.name
//
//            if (property.javaField!!.type == String::class.java) {
//                type = "varchar(50)"
//            }
//            if( propertyType == Int::class.java || propertyType.name == "java.lang.Integer"){
//                type = "int"
//            }
//
//            if( propertyType == LocalDateTime::class.java || propertyType == Date::class.java){
//                type = "DateTime"
//            }
//
//            if( propertyType == Boolean::class.java || propertyType.name  == "java.lang.Boolean"){
//                type = "bit"
//            }
//
//            return@map """`${propertyType.name}` ${type} not null ${if( propertyType.IsNumberType()) "default '0'" else if  ( propertyType.IsStringType() ) "default ''" else "" } comment ''"""
//        }
//
//        println("""
//DROP TABLE IF EXISTS `${entity.simpleName}`;
//CREATE TABLE IF NOT EXISTS `${entity.simpleName}` (
//  ${list.map { it + line_break + "," }.joinToString("")}
//  PRIMARY KEY (`id`)
//) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8 COMMENT='';
//"""
//        )
//    }
//}
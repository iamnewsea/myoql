package nbcp.myoql.code.generator.db.mysql.model

open class TableColumnMetaData {

    var tableName = ""
    var columnName = ""

    /**
     * varchar
     */
    var dataType = ""

    /**
     * varchar(20)
     */
    var columnType = ""

    var columnComment = ""

    var columnKey = ""

    var extra = ""
}
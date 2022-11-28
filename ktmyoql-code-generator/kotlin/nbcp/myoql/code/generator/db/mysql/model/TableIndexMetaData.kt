package nbcp.myoql.code.generator.db.mysql.model


/**
 * 联合索引，会有多条记录。
 */
class TableIndexMetaData {

    var tableName = ""
    var indexName = ""

    var seqInIndex = 0
    var columnName = ""
}
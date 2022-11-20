package nbcp.myoql.tool

class CrudCodeTemplateData(
    var group: String,
    var entityClass: Class<*>,
    var tableName: String,
    var idKey: String
)
package nbcp.db.sql


import nbcp.db.*

open abstract class SqlBaseSect(
        val key: SqlKeyEnum,
        var expression: String = ""
) {
}




class SelectSqlSect(expression: String = "") : SqlBaseSect(SqlKeyEnum.Select, expression) {
    var columns = mutableListOf<String>()
}

class FromSqlSect(expression: String = "") : SqlBaseSect(SqlKeyEnum.From, expression) {
    var tableName = ""
    var alias = ""
}

class JoinSqlSect(expression: String = "") : SqlBaseSect(SqlKeyEnum.Join, expression) {
    var tableName = ""
    var alias = ""
    var onWhere: WhereSqlSect? = null
}

class WhereSqlSect(expression: String = "") : SqlBaseSect(SqlKeyEnum.Where, expression) {
    var column: String = "";
    var op: String = "";
    var value: String = "";

    var linker: String = ""
    var child: WhereSqlSect? = null;
    var next :WhereSqlSect? = null
    //额外的 tables,如 in 语句。
    var extra_tables = mutableListOf<String>()
}

class GroupBySqlSect(expression: String = "") : SqlBaseSect(SqlKeyEnum.GroupBy, expression) {
    var columns = mutableListOf<String>()
}

class OrderBySqlSect(expression: String = "") : SqlBaseSect(SqlKeyEnum.OrderBy, expression) {
    var groups = mutableListOf<String>()
}

class HavingSqlSect(expression: String = "") : SqlBaseSect(SqlKeyEnum.Having, expression) {
    var where: WhereSqlSect? = null
}

class LimitSqlSect(expression: String = "") : SqlBaseSect(SqlKeyEnum.Limit, expression) {
    var skip = ""
    var take = ""
}
class OffsetSqlSect(expression: String = "") : SqlBaseSect(SqlKeyEnum.Offset, expression) {
    var skip = ""
    var take = ""
}
class WithSqlSect(expression: String = "") : SqlBaseSect(SqlKeyEnum.With, expression) {
    var name = ""
    var columns = mutableListOf<String>()
    var asSelect: SelectSqlSect? = null

    var next: WithSqlSect? = null
}

class CallSqlSect(expression: String = "") : SqlBaseSect(SqlKeyEnum.Call, expression) {
    var name = ""
    var parameters = mutableListOf<String>()
}

class IntoSqlSect(expression: String = "") : SqlBaseSect(SqlKeyEnum.Into, expression) {
    var name = ""
}

class InsertSqlSect(expression: String = "") : SqlBaseSect(SqlKeyEnum.Insert, expression) {
    var tableName = ""
    var columns = mutableListOf<String>()
}

class ValuesSqlSect(expression: String = "") : SqlBaseSect(SqlKeyEnum.Values, expression) {
    var values = mutableListOf<String>()

    var next: ValuesSqlSect? = null
}

class UpdateSqlSect(expression: String = "") : SqlBaseSect(SqlKeyEnum.Update, expression) {
    var tableName = ""
    var alias = ""
}

class SetSqlSect(expression: String = "") : SqlBaseSect(SqlKeyEnum.Set, expression) {
    var column = ""
    var value = ""
    var next: SetSqlSect? = null
}

class DeleteSqlSect(expression: String = "") : SqlBaseSect(SqlKeyEnum.Delete, expression) {
    var tableName = ""
    var alias = ""
}


class UnionSqlSect(expression: String = "") : SqlBaseSect(SqlKeyEnum.Union, expression) {
}

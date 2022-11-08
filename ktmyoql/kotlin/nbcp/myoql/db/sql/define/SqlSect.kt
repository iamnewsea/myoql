package nbcp.myoql.db.sql.define


import nbcp.myoql.db.sql.enums.SqlKeyEnum

/**
 * Sql片断基类
 */
open abstract class SqlBaseSect @JvmOverloads constructor(
    val key: SqlKeyEnum,
    var expression: String = ""
) {
}


/**
 * Select 片断
 */
class SelectSqlSect @JvmOverloads constructor(expression: String = "") : SqlBaseSect(SqlKeyEnum.Select, expression) {
    var columns = mutableListOf<String>()
}

/**
 * From 片断
 */
class FromSqlSect @JvmOverloads constructor(expression: String = "") : SqlBaseSect(SqlKeyEnum.From, expression) {
    var tableName = ""
    var alias = ""
}

/**
 * Join 片断
 */
class JoinSqlSect @JvmOverloads constructor(expression: String = "") : SqlBaseSect(SqlKeyEnum.Join, expression) {
    var tableName = ""
    var alias = ""
    var onWhere: WhereSqlSect? = null
}

/**
 * Where 片断
 */
class WhereSqlSect @JvmOverloads constructor(expression: String = "") : SqlBaseSect(SqlKeyEnum.Where, expression) {
    var column: String = "";
    var op: String = "";
    var value: String = "";

    var linker: String = ""
    var child: WhereSqlSect? = null;
    var next : WhereSqlSect? = null
    //额外的 tables,如 in 语句。
    var extra_tables = mutableListOf<String>()
}

/**
 * GroupBy片断
 */
class GroupBySqlSect @JvmOverloads constructor(expression: String = "") : SqlBaseSect(SqlKeyEnum.GroupBy, expression) {
    var columns = mutableListOf<String>()
}

/**
 * OrderBy片断
 */
class OrderBySqlSect @JvmOverloads constructor(expression: String = "") : SqlBaseSect(SqlKeyEnum.OrderBy, expression) {
    var groups = mutableListOf<String>()
}

/**
 * Having 片断
 */
class HavingSqlSect @JvmOverloads constructor(expression: String = "") : SqlBaseSect(SqlKeyEnum.Having, expression) {
    var where: WhereSqlSect? = null
}

/**
 * limit 片断
 */
class LimitSqlSect @JvmOverloads constructor(expression: String = "") : SqlBaseSect(SqlKeyEnum.Limit, expression) {
    var skip = ""
    var take = ""
}

/**
 * offset 片断
 */
class OffsetSqlSect @JvmOverloads constructor(expression: String = "") : SqlBaseSect(SqlKeyEnum.Offset, expression) {
    var skip = ""
    var take = ""
}

/**
 * with 片断
 */
class WithSqlSect @JvmOverloads constructor(expression: String = "") : SqlBaseSect(SqlKeyEnum.With, expression) {
    var name = ""
    var columns = mutableListOf<String>()
    var asSelect: SelectSqlSect? = null

    var next: WithSqlSect? = null
}

/**
 * call 片断
 */
class CallSqlSect @JvmOverloads constructor(expression: String = "") : SqlBaseSect(SqlKeyEnum.Call, expression) {
    var name = ""
    var parameters = mutableListOf<String>()
}

/**
 * into 片断
 */
class IntoSqlSect @JvmOverloads constructor(expression: String = "") : SqlBaseSect(SqlKeyEnum.Into, expression) {
    var name = ""
}

/**
 * insert 片断
 */
class InsertSqlSect @JvmOverloads constructor(expression: String = "") : SqlBaseSect(SqlKeyEnum.Insert, expression) {
    var tableName = ""
    var columns = mutableListOf<String>()
}

/**
 * values 片断
 */
class ValuesSqlSect @JvmOverloads constructor(expression: String = "") : SqlBaseSect(SqlKeyEnum.Values, expression) {
    var values = mutableListOf<String>()

    var next: ValuesSqlSect? = null
}

/**
 * update 片断
 */
class UpdateSqlSect @JvmOverloads constructor(expression: String = "") : SqlBaseSect(SqlKeyEnum.Update, expression) {
    var tableName = ""
    var alias = ""
}

/**
 * set 片断
 */
class SetSqlSect @JvmOverloads constructor(expression: String = "") : SqlBaseSect(SqlKeyEnum.Set, expression) {
    var column = ""
    var value = ""
    var next: SetSqlSect? = null
}

/**
 * delete 片断
 */
class DeleteSqlSect @JvmOverloads constructor(expression: String = "") : SqlBaseSect(SqlKeyEnum.Delete, expression) {
    var tableName = ""
    var alias = ""
}

/**
 * union 片断
 */
class UnionSqlSect @JvmOverloads constructor(expression: String = "") : SqlBaseSect(SqlKeyEnum.Union, expression) {
}

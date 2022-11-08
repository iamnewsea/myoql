package nbcp.myoql.db.sql.component

abstract class SqlBaseExecuteClip(tableName: String) : SqlBaseClip(tableName) {
    abstract fun exec(): Int
}

package nbcp.db.sql

abstract class SqlBaseExecuteClip(tableName: String) : SqlBaseClip(tableName) {
    abstract fun exec(): Int
}

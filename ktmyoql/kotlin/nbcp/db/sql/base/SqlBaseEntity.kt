package nbcp.db.sql


abstract class SqlBaseTable<T : ISqlDbEntity>(val tableClass: Class<T>, tableName: String)
    : BaseDbEntity(tableName) {
    abstract fun getUks(): Array<Array<String>>
    abstract fun getFks(): Array<FkDefine>
    abstract fun getRks(): Array<Array<String>>
    abstract fun getAutoIncrementKey(): String

    fun getColumns(): SqlColumnNames {
        var ret = SqlColumnNames()
        ret.addAll(this::class.java.declaredFields
                .filter { it.type == SqlColumnName::class.java }
                .map { it.isAccessible = true; it.get(this) as SqlColumnName }
        )

        return ret;
    }
}

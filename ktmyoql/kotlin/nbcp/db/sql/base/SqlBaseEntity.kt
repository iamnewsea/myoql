package nbcp.db.sql

import nbcp.db.sql.BaseDbEntity


abstract class SqlBaseTable<T : IBaseDbEntity>(val tableClass: Class<T>, tableName: String, val datasourceName: String = "")
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

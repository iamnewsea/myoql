package nbcp.db.mysql

import org.slf4j.LoggerFactory
import nbcp.base.extend.AsString
import nbcp.db.db
import nbcp.db.sql.*

//查询原生表。
class RawQuerySqlClip(var sql: SingleSqlData, var mainEntity: SqlBaseTable<*>? = null) : SqlBaseQueryClip(mainEntity) {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    override fun toSql(): SingleSqlData {
        return this.sql;
    }
}


class RawExecuteSqlClip(var sql: SingleSqlData, var mainEntity: SqlBaseTable<*>? = null) : SqlBaseExecuteClip(mainEntity) {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    override fun toSql(): SingleSqlData {
        return this.sql;
    }

    override fun exec(): Int {
        var sql = toSql()
        var executeData = sql.toExecuteSqlAndParameters();
        var params = executeData.parameters.map { it.value }.toTypedArray()

        var msg_log = mutableListOf("[sql] ${executeData.executeSql}", "[参数] ${params.map { it.AsString() }.joinToString(",")}")
        var startAt = System.currentTimeMillis();

        var n = 0;
        try {
            n = jdbcTemplate.update(executeData.executeSql, *params)
            msg_log.add("[耗时] ${System.currentTimeMillis() - startAt} ms")
            logger.info(msg_log.joinToString("\n"))
        } catch (e: Exception) {
            msg_log.add("""[错误] ${e.message}
${e.stackTrace.map { "\t" + it.toString() }}
""")
            logger.error(msg_log.joinToString("\n"))
            throw e;
        }


        db.affectRowCount = n
        return n
    }
}
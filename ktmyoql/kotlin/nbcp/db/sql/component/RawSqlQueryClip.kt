package nbcp.db.sql

import org.slf4j.LoggerFactory
import nbcp.base.extend.AsString
import nbcp.base.extend.InfoError
import nbcp.base.line_break
import nbcp.db.db
import nbcp.db.sql.*

//查询原生表。
class RawQuerySqlClip(var sql: SingleSqlData, var mainEntity: SqlBaseTable<*> ) : SqlBaseQueryClip(mainEntity) {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    override fun toSql(): SingleSqlData {
        return this.sql;
    }
}


class RawExecuteSqlClip(var sql: SingleSqlData, var mainEntity: SqlBaseTable<*> ) : SqlBaseExecuteClip(mainEntity) {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    override fun toSql(): SingleSqlData {
        return this.sql;
    }

    override fun exec(): Int {
        db.affectRowCount = -1;
        var sql = toSql()
        var executeData = sql.toExecuteSqlAndParameters();

        var startAt = System.currentTimeMillis();

        var n = -1;
        try {
            n = jdbcTemplate.update(executeData.executeSql, *executeData.executeParameters)
        } catch (e: Exception) {
            throw e;
        } finally {
            logger.InfoError(n < 0) {
                var msg_log = mutableListOf("[sql] ${executeData.executeSql}", "[参数] ${executeData.executeParameters.map { it.AsString() }.joinToString(",")}")
                msg_log.add("[耗时] ${System.currentTimeMillis() - startAt} ms")

                return@InfoError msg_log.joinToString(line_break)
            }
        }


        db.affectRowCount = n
        return n
    }
}
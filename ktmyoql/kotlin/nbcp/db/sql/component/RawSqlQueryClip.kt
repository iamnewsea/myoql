package nbcp.db.sql

import org.slf4j.LoggerFactory
import nbcp.comm.AsString
import nbcp.comm.InfoError
import nbcp.comm.*

import nbcp.db.db
import nbcp.db.sql.*
import java.time.LocalDateTime

//查询原生表。
class RawQuerySqlClip(var sql: SingleSqlData, tableName: String) : SqlBaseQueryClip(tableName) {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    override fun toSql(): SingleSqlData {
        return this.sql;
    }
}


class RawExecuteSqlClip(var sql: SingleSqlData, tableName: String) : SqlBaseExecuteClip(tableName) {
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

        var startAt = LocalDateTime.now()

        var n = -1;
        try {
            n = jdbcTemplate.update(executeData.executeSql, *executeData.executeParameters)
            db.executeTime = LocalDateTime.now() - startAt
        } catch (e: Exception) {
            throw e;
        } finally {
            logger.InfoError(n < 0) {
                var msg_log = mutableListOf(
                        "[sql] ${executeData.executeSql}",
                        "[参数] ${executeData.executeParameters.map { it.AsString() }.joinToString(",")}",
                        "[result] ${n}",
                        "[耗时] ${db.executeTime}")

                return@InfoError msg_log.joinToString(line_break)
            }
        }


        db.affectRowCount = n
        return n
    }
}
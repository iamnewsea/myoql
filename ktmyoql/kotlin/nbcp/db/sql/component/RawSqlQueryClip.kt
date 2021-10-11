package nbcp.db.sql

import org.slf4j.LoggerFactory
import nbcp.comm.*

import nbcp.db.db
import nbcp.db.sql.*
import java.time.LocalDateTime

//查询原生表。
class RawQuerySqlClip(var sql: SingleSqlData, tableName: String) : SqlBaseQueryClip(tableName) {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    constructor(sqlWithVar: String, sqlValue: JsonMap = JsonMap(), tableName: String = "")
            : this(SingleSqlData(sqlWithVar, sqlValue), tableName) {

    }

    override fun toSql(): SingleSqlData {
        return this.sql;
    }
}


class RawExecuteSqlClip(var sql: SingleSqlData, tableName: String) : SqlBaseExecuteClip(tableName) {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    constructor(sqlWithVar: String, sqlValue: JsonMap = JsonMap(), tableName: String = "")
            : this(SingleSqlData(sqlWithVar, sqlValue), tableName) {

    }

    override fun toSql(): SingleSqlData {
        return this.sql;
    }

    override fun exec(): Int {
        db.affectRowCount = -1;
        var sql = toSql()
        var executeData = sql.toExecuteSqlAndParameters();

        val startAt = LocalDateTime.now()

        var n = -1;
        var error:Exception? = null;
        try {
            n = jdbcTemplate.update(executeData.executeSql, executeData.executeParameters)
            db.executeTime = LocalDateTime.now() - startAt
        } catch (e: Exception) {
            error = e;
            throw e;
        } finally {
            SqlLogger.logExec(error  ,tableName,executeData, n );
        }


        db.affectRowCount = n
        return n
    }
}
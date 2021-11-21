package nbcp.db.sql

import org.slf4j.LoggerFactory
import nbcp.comm.*

import nbcp.db.db
import java.time.LocalDateTime

//查询原生表。
class RawQuerySqlClip(var sqlParameter: SqlParameterData, tableName: String) : SqlBaseQueryClip(tableName) {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    constructor(sqlWithVar: String, sqlValue: JsonMap = JsonMap(), tableName: String = "")
            : this(SqlParameterData(sqlWithVar, sqlValue), tableName) {

    }

    override fun toSql(): SqlParameterData {
        return this.sqlParameter;
    }
}


class RawExecuteSqlClip(var sqlParameter: SqlParameterData, tableName: String) : SqlBaseExecuteClip(tableName) {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    constructor(sqlWithVar: String, sqlValue: JsonMap = JsonMap(), tableName: String = "")
            : this(SqlParameterData(sqlWithVar, sqlValue), tableName) {

    }

    override fun toSql(): SqlParameterData {
        return this.sqlParameter;
    }

    override fun exec(): Int {
        db.affectRowCount = -1;
        var sql = toSql()
//        var executeData = sql.toExecuteSqlAndParameters();

        val startAt = LocalDateTime.now()

        var n = -1;
        var error:Exception? = null;
        try {
            n = jdbcTemplate.update(sql.expression, sql.values)
            db.executeTime = LocalDateTime.now() - startAt
        } catch (e: Exception) {
            error = e;
            throw e;
        } finally {
            SqlLogger.logExec(error  ,tableName,sql, n );
        }


        db.affectRowCount = n
        return n
    }
}
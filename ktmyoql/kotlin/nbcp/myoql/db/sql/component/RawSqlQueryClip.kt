package nbcp.myoql.db.sql.component

import nbcp.base.comm.JsonMap
import nbcp.myoql.db.sql.base.SqlParameterData
import org.slf4j.LoggerFactory

/**
 * 使用 NamedParameterJdbcTemplate 原生查询dql语句(select)
 */
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



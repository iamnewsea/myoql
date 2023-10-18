package nbcp.myoql.db.sql.component

import nbcp.base.extend.minus
import nbcp.myoql.db.db
import nbcp.myoql.db.sql.base.SqlBaseMetaTable
import nbcp.myoql.db.sql.base.SqlParameterData
import nbcp.myoql.db.sql.extend.quoteTableName
import nbcp.myoql.db.sql.logDelete
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import java.io.Serializable
import java.time.LocalDateTime

/**
 * Created by yuxh on 2018/7/2
 */

class SqlDeleteClip<M : SqlBaseMetaTable<out Serializable>>(var mainEntity: M) :
    SqlBaseExecuteClip(mainEntity.tableName) {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    private var take = -1;

    val whereDatas = WhereData()

    fun where(whereData: (M) -> WhereData): SqlDeleteClip<M> {
        this.whereDatas.and(whereData(this.mainEntity));
        return this;
    }

    /**
     * delete from table where id=1 limit n;
     */
    fun limit(take: Int): SqlDeleteClip<M> {
        this.take = take;
        return this;
    }

    override fun toSql(): SqlParameterData {
        if (whereDatas.hasValue == false) {
            throw RuntimeException("不允许执行没有 where 条件的 delete ${mainEntity.tableName} 语句")
        }

        var where = whereDatas.toSingleData();
        var exp = "delete from ${mainEntity.quoteTableName} where ${where.expression}";
        var values = where.values

        if (this.take >= 0) {
            exp += " limit ${take}"
        }

        var execute = SqlParameterData(exp, values)


//        if( logger.isInfoEnabled){
//            logger.info(execute.expression + line_break + "\t"+ execute.values.ToJson())
//        }

        return execute
    }

    override fun exec(): Int {
        db.affectRowCount = -1;
        var settings = db.sql.sqlEvents?.onDeleting(this) ?: arrayOf();
        if (settings.any { it.second.result == false }) {
            return 0;
        }

        val sql = toSql()
//        val executeData = sql.toExecuteSqlAndParameters();
        val startAt = LocalDateTime.now();

        var n = -1;
        var error: Exception? = null;
        try {
            n = jdbcTemplate.update(sql.expression, MapSqlParameterSource(sql.values))
            db.executeTime = LocalDateTime.now() - startAt

//            if (n > 0) {
//                cacheService.delete4BrokeCache(sql)
//            }
        } catch (e: Exception) {
            error = e;
            throw e;
        } finally {
            logger.logDelete(error, tableName, sql, n);
        }

        settings.forEach {
            it.first.delete(this, it.second)
        }

        db.affectRowCount = n
        return n
    }
}
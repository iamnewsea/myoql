package nbcp.db.sql

import org.slf4j.LoggerFactory
import nbcp.comm.*
import nbcp.base.extend.AsString
import nbcp.base.extend.Error
import nbcp.base.extend.Info
import nbcp.base.extend.InfoError

import nbcp.db.db
import nbcp.db.sql.*
import java.time.LocalDateTime

/**
 * Created by yuxh on 2018/7/2
 */

class SqlDeleteClip<M : SqlBaseTable<out T>, T : IBaseDbEntity>(var mainEntity: M) : SqlBaseExecuteClip(mainEntity.tableName) {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    val whereDatas = WhereData()

    fun where(whereData: (M) -> WhereData): SqlDeleteClip<M, T> {
        this.whereDatas.and(whereData(this.mainEntity));
        return this;
    }

    override fun toSql(): SingleSqlData {
        if (whereDatas.hasValue == false) {
            throw RuntimeException("不允许执行没有 where 条件的 delete ${mainEntity.tableName} 语句")
        }

        var where = whereDatas.toSingleData();
        var exp = "delete from ${mainEntity.quoteTableName} where ${where.expression}";
        var values = where.values

        var execute = SingleSqlData(exp, values)

//        if( logger.isInfoEnabled){
//            logger.info(execute.expression + line_break + "\t"+ execute.values.ToJson())
//        }

        return execute
    }

    override fun exec(): Int {
        db.affectRowCount = -1;
        var settings = db.sql.sqlEvents.onDeleting(this);
        if( settings.any{it.second != null && it.second!!.result ==false}){
            return 0;
        }

        var sql = toSql()
        var executeData = sql.toExecuteSqlAndParameters();
        var startAt = LocalDateTime.now();

        var n = -1;
        try {
            n = jdbcTemplate.update(executeData.executeSql, *executeData.executeParameters)
            db.executeTime = LocalDateTime.now() - startAt

            if (n > 0) {
                cacheService.delete4BrokeCache(sql)
            }
        } catch (e: Exception) {
            throw e;
        } finally {
            logger.InfoError(n < 0) {
                var msg_log = mutableListOf("[sql] ${executeData.executeSql}", "[参数] ${executeData.executeParameters.map { it.AsString() }.joinToString(",")}")
                msg_log.add("[耗时] ${db.executeTime}")
                return@InfoError msg_log.joinToString(line_break)
            }
        }

        settings.forEach {
            it.first.delete(this,it.second)
        }

        db.affectRowCount = n
        return n
    }
}
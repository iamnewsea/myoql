package nbcp.db.sql

import org.slf4j.LoggerFactory
import nbcp.comm.*

import nbcp.db.db
import java.time.LocalDateTime

/**
 * Created by yuxh on 2018/7/2
 */

class SqlDeleteClip<M : SqlBaseMetaTable<out T>, T : ISqlDbEntity>(var mainEntity: M) : SqlBaseExecuteClip(mainEntity.tableName) {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    private var take =-1;

    val whereDatas = WhereData()

    fun where(whereData: (M) -> WhereData): SqlDeleteClip<M, T> {
        this.whereDatas.and(whereData(this.mainEntity));
        return this;
    }

    /**
     * delete from table where id=1 limit n;
     */
    fun limit(take:Int):SqlDeleteClip<M, T>{
        this.take = take;
        return this;
    }

    override fun toSql(): SingleSqlData {
        if (whereDatas.hasValue == false) {
            throw RuntimeException("不允许执行没有 where 条件的 delete ${mainEntity.tableName} 语句")
        }

        var where = whereDatas.toSingleData();
        var exp = "delete from ${mainEntity.quoteTableName} where ${where.expression}";
        var values = where.values

        if( this.take >=0){
            exp += " limit ${take}"
        }

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

//            if (n > 0) {
//                cacheService.delete4BrokeCache(sql)
//            }
        } catch (e: Exception) {
            throw e;
        } finally {
            logger.InfoError(n < 0) {
                var msg_log = mutableListOf("" +
                        "[delete] ${executeData.executeSql}",
                        "[参数] ${executeData.executeParameters.map { it.AsString() }.joinToString(",")}",
                        "[result] ${n}",
                        "[耗时] ${db.executeTime}")

                return@InfoError msg_log.joinToString(const.line_break)
            }
        }

        settings.forEach {
            it.first.delete(this,it.second)
        }

        db.affectRowCount = n
        return n
    }
}
package nbcp.db.mysql

import org.slf4j.LoggerFactory
import nbcp.base.comm.JsonMap
import nbcp.base.extend.AsString
import nbcp.db.db
import nbcp.db.sql.*

/**
 * Created by yuxh on 2018/7/2
 */

class SqlDeleteClip<M : SqlBaseTable<out T>, T : IBaseDbEntity>(var mainEntity: M) : SqlBaseExecuteClip(mainEntity) {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    private var whereDatas = WhereData()

    fun where(whereData: (M) -> WhereData): SqlDeleteClip<M, T> {
        this.whereDatas.and(whereData(this.mainEntity));
        return this;
    }

    override fun toSql(): SingleSqlData {
        if (whereDatas.hasValue == false) {
            throw Exception("不允许执行没有 where 条件的 delete ${mainEntity.tableName} 语句")
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

        if (n > 0) {
            cacheService.delete4BrokeCache(sql)
        }

        db.affectRowCount = n
        return n
    }
}
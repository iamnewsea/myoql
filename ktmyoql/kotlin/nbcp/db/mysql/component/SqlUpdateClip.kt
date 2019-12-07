package nbcp.db.mysql

import nbcp.comm.*
import org.slf4j.LoggerFactory
import nbcp.db.*
import nbcp.db.sql.*
import nbcp.base.extend.*
import nbcp.base.utils.MyUtil
import java.io.Serializable


/**
 * Created by yuxh on 2018/7/2
 */

class SqlUpdateClip<M : SqlBaseTable<out T>, T : IBaseDbEntity>(var mainEntity: M) : SqlBaseExecuteClip(mainEntity) {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    private var whereDatas = WhereData()
    private val sets = linkedMapOf<SqlColumnName, Any?>()
    private val joins = mutableListOf<JoinTableData<*, *>>()

    fun <M2 : SqlBaseTable<out T2>, T2 : IBaseDbEntity> join(joinTable: M2, onWhere: (M, M2) -> WhereData): SqlUpdateClip<M, T> {
        this.joins.add(JoinTableData("join", joinTable, onWhere(this.mainEntity, joinTable), SqlColumnNames()))
        return this
    }

    fun where(whereData: (M) -> WhereData): SqlUpdateClip<M, T> {
        this.whereDatas.and(whereData(this.mainEntity));
        return this;
    }

    fun set(set: (M) -> Pair<SqlColumnName, Serializable>): SqlUpdateClip<M, T> {
        var p = set(this.mainEntity)
        this.sets.put(p.first, proc_value(p.second))
        return this
    }

    fun unset(set: (M) -> SqlColumnName): SqlUpdateClip<M, T> {
        var p = set(this.mainEntity)
        this.sets.remove(p)
        return this
    }

    fun set(entity: T, whereKey: ((M) -> SqlColumnNames)): SqlUpdateClip<M, T> {
        var columns = this.mainEntity.getColumns()
        var field_names = entity::class.java.AllFields.map { it.name };

        var whereColumns = whereKey(this.mainEntity)
        var where = WhereData();

        whereColumns.forEach { column ->
            var value = MyUtil.getPrivatePropertyValue(entity, column.name)

            where.and(WhereData("${column.fullName} = #${column.jsonKeyName}@", JsonMap(column.jsonKeyName to value)))
        }

        //自增 id 不能更新。
        var auKey = this.mainEntity.getAutoIncrementKey();
        columns.minus(whereColumns)
                .filter { column -> column.name != auKey && field_names.contains(column.name) }
                .forEach { key ->
                    var value = MyUtil.getPrivatePropertyValue(entity, key.name)
                    if (value == null) {
                        return@forEach
                    }

                    this.sets.put(key, proc_value(value));
                }

        this.whereDatas.and(where)
        return this
    }

    override fun toSql(): SingleSqlData {
        if (whereDatas.hasValue == false) {
            throw Exception("不允许执行没有 where 条件的 update ${mainEntity.tableName} 语句")
        }

        if (sets.any() == false) {
            throw Exception("update ${mainEntity.tableName}  where 需要 set 语句")
        }

        var ret = SingleSqlData();
        ret.expression += "update ${mainEntity.quoteTableName} ";

        var hasAlias = false;
        if (this.mainEntity.getAliaTableName().HasValue && (this.mainEntity.getAliaTableName() != this.mainEntity.tableName)) {
            ret.expression += " as " + db.getQuoteName(this.mainEntity.getAliaTableName());
            hasAlias = true;
        }

        joins.forEach {
            ret.expression += " ${it.joinType} ${it.joinTable.selectSql} on ("

            ret += it.onWhere.toSingleData()

            ret.expression += ") "

            hasAlias = true;
        }


        ret.expression += " set "

//        var tab_converter = dbr.converter.filter { it.key.tableName == this.mainEntity.tableName }
//                .mapKeys { it.key.name }

        sets.keys.forEachIndexed { index, setKey ->
            var setValue = sets.get(setKey)

            if (index > 0) {
                ret.expression += " , "
            }

            if (setValue is SqlColumnName) {
                if (hasAlias == false) {
                    setKey.tableName = "";
                    setValue.tableName = "";
                }

                ret.expression += setKey.fullName + "=" + setValue.fullName
            } else {
                if (hasAlias == false) {
                    setKey.tableName = "";
                }

//                if (setValue != null && tab_converter.any() &&  tab_converter.contains(  setKey.name)) {
//                    setValue = tab_converter.get(setKey.name)?.convert(setValue.toString()) ?: setValue
//                }

                ret += SingleSqlData(setKey.fullName + " = #${setKey.jsonKeyName}@", JsonMap(setKey.jsonKeyName to setValue))
            }
        }


        ret.expression += " where "
        ret += whereDatas.toSingleData();

        return ret
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
            cacheService.updated4BrokeCache(sql)
        }
        db.affectRowCount = n;
        return n;
    }
}
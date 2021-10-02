package nbcp.db.sql

import nbcp.comm.*
import org.slf4j.LoggerFactory
import nbcp.db.*

import nbcp.utils.*
import java.io.Serializable
import java.time.LocalDateTime


/**
 * Created by yuxh on 2018/7/2
 */

open class SqlUpdateClip<M : SqlBaseMetaTable<out java.io.Serializable> >(var mainEntity: M) : SqlBaseExecuteClip(mainEntity.tableName) {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    val whereDatas = WhereData()
    val sets = linkedMapOf<SqlColumnName, Any?>()
    //默认是-1
    private var take = -1;
    private val joins = mutableListOf<JoinTableData<*, *>>()

    fun <M2 : SqlBaseMetaTable<out T2>, T2 : java.io.Serializable> join(joinTable: M2, onWhere: (M, M2) -> WhereData): SqlUpdateClip<M> {
        this.joins.add(JoinTableData("join", joinTable, onWhere(this.mainEntity, joinTable), SqlColumnNames()))
        return this
    }

    fun where(whereData: (M) -> WhereData): SqlUpdateClip<M> {
        this.whereDatas.and(whereData(this.mainEntity));
        return this;
    }

    fun set(set: (M) -> Pair<SqlColumnName, Serializable?>): SqlUpdateClip<M> {
        var p = set(this.mainEntity)
        if(p.second == null){
            this.sets.put(p.first,null);
        }
        else {
            this.sets.put(p.first, proc_value(p.second!!))
        }
        return this
    }

    fun unset(set: (M) -> SqlColumnName): SqlUpdateClip<M> {
        var p = set(this.mainEntity)
        this.sets.remove(p)
        return this
    }

    /**
     * update table set column=value where id=1 limit n;
     */
    fun limit(take:Int):SqlUpdateClip<M>{
        this.take = take;
        return this;
    }

//    fun set(entity: T, whereKey: ((M) -> SqlColumnNames)): SqlUpdateClip<M, T> {
//        var columns = this.mainEntity.getColumns()
//        var field_names = entity::class.java.AllFields.map { it.name };
//
//        var whereColumns = whereKey(this.mainEntity)
//        var where = WhereData();
//
//        whereColumns.forEach { column ->
//            var value = MyUtil.getPrivatePropertyValue(entity, column.name)
//
//            where.and(WhereData("${column.fullName} = {${column.jsonKeyName}}", JsonMap(column.jsonKeyName to value)))
//        }
//
//        //自增 id 不能更新。
//        var auKey = this.mainEntity.getAutoIncrementKey();
//        columns.minus(whereColumns)
//                .filter { column -> column.name != auKey && field_names.contains(column.name) }
//                .forEach { key ->
//                    var value = MyUtil.getPrivatePropertyValue(entity, key.name)
//                    if (value == null) {
//                        return@forEach
//                    }
//
//                    this.sets.put(key, proc_value(value));
//                }
//
//        this.whereDatas.and(where)
//        return this
//    }

    override fun toSql(): SingleSqlData {
        if (whereDatas.hasValue == false) {
            throw RuntimeException("不允许执行没有 where 条件的 update ${mainEntity.tableName} 语句")
        }

        if (sets.any() == false) {
            throw RuntimeException("update ${mainEntity.tableName}  where 需要 set 语句")
        }

        var ret = SingleSqlData();
        ret.expression += "update ${mainEntity.quoteTableName} ";

        var hasAlias = false;
        if (this.mainEntity.getAliaTableName().HasValue && (this.mainEntity.getAliaTableName() != this.mainEntity.tableName)) {
            ret.expression += " as " + db.sql.getSqlQuoteName(this.mainEntity.getAliaTableName());
            hasAlias = true;
        }

        joins.forEach {
            ret.expression += " ${it.joinType} ${it.joinTable.fromTableName} on ("

            ret += it.onWhere.toSingleData()

            ret.expression += ") "

            hasAlias = true;
        }


        ret.expression += " set "

//        var tab_converter = dbr.converter.filter { it.key.tableName == this.mainEntity.tableName }
//                .mapKeys { it.key.name }

        sets.keys.forEachIndexed { index, setKey ->
            var setValue = sets.filterKeys { it.name == setKey.name }.values.firstOrNull()

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

                ret += SingleSqlData(setKey.fullName + " = {${setKey.jsonKeyName}}", JsonMap(setKey.jsonKeyName to setValue))
            }
        }


        ret.expression += " where "
        ret += whereDatas.toSingleData();


        if( this.take >=0){
            ret.expression += " limit ${take}"
        }
        return ret
    }

    override fun exec(): Int {
        db.affectRowCount = -1;

        var settings = db.sql.sqlEvents.onUpdating(this);
        if (settings.any { it.second != null && it.second!!.result == false }) {
            return 0;
        }

        var sql = toSql()
        var executeData = sql.toExecuteSqlAndParameters();

        var startAt = LocalDateTime.now();

        var error: Exception? = null;
        var n = -1;
        try {
            n = jdbcTemplate.update(executeData.executeSql, *executeData.executeParameters)
            db.executeTime = LocalDateTime.now() - startAt
//            if (n > 0) {
//                cacheService.updated4BrokeCache(sql)
//            }
        } catch (e: Exception) {
            error = e;
            throw e;
        } finally {
            SqlLogger.logUpdate(error,tableName,executeData,n );
        }

        settings.forEach {
            it.first.update(this, it.second)
        }

        db.affectRowCount = n;
        return n;
    }
}
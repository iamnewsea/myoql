package nbcp.db.sql

import org.slf4j.LoggerFactory
import nbcp.comm.*
import nbcp.comm.*
import nbcp.base.extend.*
import nbcp.base.line_break
import nbcp.db.*
import nbcp.db.sql.*
import kotlin.reflect.full.memberProperties


class SqlQueryClip<M : SqlBaseTable<out T>, T : IBaseDbEntity>(var mainEntity: M) : SqlBaseQueryClip(mainEntity) {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    private var whereDatas = WhereData()
    val columns = SqlColumnNames()
    val joins = mutableListOf<JoinTableData<*, *>>()
    val orders = mutableListOf<SqlOrderBy>()
    private val groups = mutableListOf<SingleSqlData>()
    private val having = WhereData()
    private var subSelect: SqlQueryClip<*, *>? = null //<out SqlBaseTable<out IBaseDbEntity>, out IBaseDbEntity>? = null
    private var subSelectAlias: String = ""


    fun wrapSelect(alias: String): SqlQueryClip<M, T> {
        var ret = SqlQueryClip(this.mainEntity)
        ret.subSelect = this;
        ret.subSelectAlias = alias;
        return ret;
    }

    fun distinct(): SqlQueryClip<M, T> {
        this.distinct = true;
        return this;
    }

    fun where(whereData: (M) -> WhereData): SqlQueryClip<M, T> {
        this.whereDatas.and(whereData(this.mainEntity));
        return this;
    }

    fun select(selectColumn: (M) -> SqlColumnNames): SqlQueryClip<M, T> {
        this.columns.addAll(selectColumn(this.mainEntity))
        return this;
    }

    fun selectOne(selectColumn: (M) -> SqlColumnName): SqlQueryClip<M, T> {
        this.columns.add(selectColumn(this.mainEntity))
        return this;
    }

    fun groupBy(group: (M) -> SingleSqlData): SqlQueryClip<M, T> {
        this.groups.add(group(this.mainEntity))
        return this;
    }

    fun having(having: (M) -> WhereData): SqlQueryClip<M, T> {
        this.having.and(having(this.mainEntity));
        return this;
    }

    fun limit(skip: Int, take: Int): SqlQueryClip<M, T> {
        this.skip = skip;
        this.take = take;
        return this;
    }

    fun skip(skip: Int): SqlQueryClip<M, T> {
        this.skip = skip;
        return this;
    }

    fun take(take: Int): SqlQueryClip<M, T> {
        this.take = take;
        return this;
    }

    private fun <M2 : SqlBaseTable<out T2>, T2 : IBaseDbEntity> getJoinOnWhere(joinTable: M2): WhereData {

        var fks = this.mainEntity.getFks().filter { it.refTable == joinTable.tableName }
        if (fks.size == 0) {
            throw RuntimeException("找不到 ${this.mainEntity.tableName}->${joinTable.tableName} 的外键定义")
        } else if (fks.size > 1) {
            throw RuntimeException("找到多个外键定义: ${this.mainEntity.tableName}->${joinTable.tableName}")
        }

        var fk = fks.first()
        return WhereData("${db.getQuoteName(fk.table)}.${db.getQuoteName(fk.column)} = ${db.getQuoteName(fk.refTable)}.${db.getQuoteName(fk.refColumn)}")
    }

    fun <M2 : SqlBaseTable<out T2>, T2 : IBaseDbEntity> join(joinTable: M2, onWhere: (M, M2) -> WhereData, select: ((M2) -> SqlColumnNames)? = null): SqlQueryClip<M, T> {
        this.joins.add(JoinTableData("join", joinTable, onWhere(this.mainEntity, joinTable), if (select == null) SqlColumnNames() else select(joinTable)))
        return this
    }

    //根据外键自动 onWhere
    fun <M2 : SqlBaseTable<out T2>, T2 : IBaseDbEntity> join(joinTable: M2, select: ((M2) -> SqlColumnNames)? = null): SqlQueryClip<M, T> {
        this.join(joinTable, { a, b -> getJoinOnWhere(joinTable) }, select)
        return this
    }

    fun <M2 : SqlBaseTable<out T2>, T2 : IBaseDbEntity> left_join(joinTable: M2, onWhere: (M, M2) -> WhereData, select: ((M2) -> SqlColumnNames)? = null): SqlQueryClip<M, T> {
        this.joins.add(JoinTableData("left join", joinTable, onWhere(this.mainEntity, joinTable), select?.invoke(joinTable)
                ?: SqlColumnNames()))
        return this
    }

    //根据外键自动 onWhere
    fun <M2 : SqlBaseTable<out T2>, T2 : IBaseDbEntity> left_join(joinTable: M2, select: ((M2) -> SqlColumnNames)? = null): SqlQueryClip<M, T> {
        this.left_join(joinTable, { a, b -> getJoinOnWhere(joinTable) }, select)
        return this
    }


    fun orderBy(order: (M) -> SqlColumnName): SqlQueryClip<M, T> {
        this.orders.add(order(this.mainEntity).asc)
        return this
    }

    fun orderByDesc(order: (M) -> SqlColumnName): SqlQueryClip<M, T> {
        this.orders.add(order(this.mainEntity).desc)
        return this
    }

    override fun toSql(): SingleSqlData {
        var ret = SingleSqlData();

        if (this.subSelect != null) {
            var selectSql = this.subSelect!!.toSql();

            ret.expression += "select ";

            if (columns.any() == false) {
                if (this.subSelect!!.columns.any() == false) {
                    ret.expression += this.subSelectAlias + ".*"
                } else {
                    ret.expression += this.subSelect!!.columns.map { this.subSelectAlias + "." + db.getQuoteName(it.getAliasName()) }.joinToString(",")
                }
            } else {
                var selectColumn = columns.map { this.subSelectAlias + "." + db.getQuoteName(it.getAliasName()) }.joinToString(",")

                ret.expression += selectColumn
            }

            joins.forEach {
                if (it.select.any()) {
                    ret.expression += "," + it.select.map { this.subSelectAlias + "." + db.getQuoteName(it.getAliasName()) }.joinToString(",")
                }
            }

            ret.expression += "from ("
            ret += selectSql

            ret.expression += ")" + (if (this.subSelectAlias.HasValue) " as " + db.getQuoteName(this.subSelectAlias) else "")

        } else {
            ret.expression += "select "

            if (this.distinct) {
                ret.expression += "distinct "
            }


            if (columns.any() == false) {
                ret.expression += this.mainEntity.quoteTableName + ".*"
            } else {
                var selectColumn = columns.toSelectSql()

                ret.expression += selectColumn
            }

            joins.forEach {
                if (it.select.any()) {
                    ret.expression += "," + it.select.toSelectSql()
                }
            }

            ret.expression += " from " + mainEntity.selectSql
        }

        joins.forEach {
            ret.expression += " ${it.joinType} ${it.joinTable.selectSql} on ("

            ret += it.onWhere.toSingleData()

            ret.expression += ")"
        }

        if (whereDatas.hasValue) {
            ret.expression += " where "
            ret += whereDatas.toSingleData()
        }

        if (groups.any()) {
            ret.expression += " group by "
            groups.forEachIndexed { index, group ->
                if (index > 0) {
                    ret.expression += ","
                }

                ret += group
            }
        }

        if (orders.any()) {
            ret.expression += " order by"
            orders.forEachIndexed { index, order ->
                if (index > 0) {
                    ret.expression += ","
                }
                ret += order.toSingleSqlData()
            }
        }

        if (having.hasValue) {
            ret.expression += " having "
            ret += having.toSingleData()
        }

        if (skip > 0 && take >= 0) {
            ret.expression += " limit ${skip},${take}"
        } else if (take >= 0) {
            ret.expression += " limit  ${take}"
        } else if (skip > 0) {
            ret.expression += " limit  ${skip},99999"
        }


        return ret
    }

    /**
     * 忽略 skip , take
     * @param countQuery 回调可以二次处理查询
     */
    fun count(countQuery: ((SqlQueryClip<M, T>) -> Unit)? = null): Int {
        var query = this.CloneObject();
        query.joins.forEach {
            it.select.clear();
        }

        query.columns.clear()
        query.columns.add(SqlColumnName.of("count(1) as cou"))
        query.skip(-1);
        query.take(-1);
        query.orders.clear()

        if (countQuery != null) {
            countQuery(query);
        }

        var executeSql = query.toSql();
        var mapList = toMapList(executeSql)

        if (!mapList.any()) {
            return 0;
        }

        return mapList.first().values.first().AsInt()
    }


    fun toList(itemFunc: ((JsonMap) -> Unit)? = null): List<T> {
        return toList(this.mainEntity.tableClass, itemFunc);
    }

    fun <R : Any> toList(entityClass: Class<R>, itemFunc: ((JsonMap) -> Unit)? = null): MutableList<R> {
        var ret = toMapList().map {
            if (itemFunc != null) {
                itemFunc(it);
            }
            return@map mapToEntity(it, { entityClass.newInstance() })
        }.toMutableList()

        return ret
    }

    fun toEntity(mapFunc: ((JsonMap) -> Unit)? = null): T? {
        return toEntity(this.mainEntity.tableClass, mapFunc)
    }

    fun <R : Any> toEntity(entityClass: Class<R>, mapFunc: ((JsonMap) -> Unit)? = null): R? {
        this.take = 1
        return toMapList().map {
            mapFunc?.invoke(it)
            mapToEntity(it, { entityClass.newInstance() })
        }
                .firstOrNull()
    }

    override fun toMap(): JsonMap? {
        this.take = 1;
        return super.toMap()
    }

    override fun exists(): Boolean {
        this.take = 1;
        this.columns.clear();
        var const1 = SqlColumnName.of(DbType.Int, "1")
        this.columns.add(const1);
        return super.exists()
    }

    fun <M2 : SqlBaseTable<out IBaseDbEntity>> insertInto(insertTable: M2): Int {
        db.affectRowCount = -1;
        var select = this

        if (select.columns.any() == false) {
            select.columns.addAll(select.mainEntity.getColumns())
        }

        select.columns.removeAll { it.getAliasName() == select.mainEntity.getAutoIncrementKey() }

        //校验, 必须是表的列.
        var surplusColumns = select.columns.map { it.getAliasName() }.minus(insertTable::class.memberProperties.map { it.name })
        if (surplusColumns.any()) {
            throw RuntimeException("插入 select 语句时,发现多余的列: ${surplusColumns.joinToString(",")}")
        }

        var exp = "insert into ${insertTable.quoteTableName} (${select.columns.map { "${db.getQuoteName(it.getAliasName())}" }.joinToString(",")}) ";

        var sql = select.toSql().toExecuteSqlAndParameters()

        var executeSql = SqlExecuteData(exp + sql.executeSql, sql.parameters)

        var n = -1;
        try {
            n = SqlBaseClip.getJdbcTemplateByDatasrouce(insertTable.datasourceName).update(executeSql.executeSql, *executeSql.parameters.map { it.value }.toTypedArray())
            if (n > 0) {
                cacheService.insertSelect4BrokeCache(insertTable.tableName)
            }
        } catch (e: Exception) {
            throw e;
        } finally {
            logger.InfoError(n < 0) {
                executeSql.executeSql + line_break + "\t" + executeSql.parameters.ToJson()
            }
        }

        db.affectRowCount = n
        return n
    }

    fun toListResult(mapFunc: ((JsonMap) -> Unit)? = null): ListResult<out T> {
        return toListResult(this.mainEntity.tableClass, mapFunc);
    }

    /**
     */
    fun <R : Any> toListResult(entityClass: Class<R>, mapFunc: ((JsonMap) -> Unit)? = null): ListResult<R> {
        var ret = ListResult<R>()
        var data = toList(entityClass, mapFunc)

        if (this.skip == 0 && this.take > 0) {
            if (data.size < this.take) {
                ret.total = data.size;
            } else {
                ret.total = count()
            }
        }

        ret.data = data;
        return ret
    }


    fun toMapListResult(): ListResult<JsonMap> {
        return toListResult(JsonMap::class.java);
    }
}

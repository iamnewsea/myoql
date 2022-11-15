package nbcp.myoql.db.sql.component

import nbcp.base.comm.JsonMap
import nbcp.base.comm.ListResult
import nbcp.base.comm.StringMap
import nbcp.base.comm.config
import nbcp.base.extend.*
import nbcp.myoql.annotation.FromRedisCache
import nbcp.myoql.annotation.onlyGetFromCache
import nbcp.myoql.annotation.onlySetToCache
import nbcp.myoql.db.db
import nbcp.myoql.db.enums.MyOqlDbScopeEnum
import nbcp.myoql.db.mongo.MongoEntityCollector
import nbcp.myoql.db.sql.base.*
import nbcp.myoql.db.sql.enums.DbType
import nbcp.myoql.db.sql.enums.SqlLockType
import nbcp.myoql.db.sql.extend.MyOqlSqlTreeData
import nbcp.myoql.db.sql.extend.fromTableName
import nbcp.myoql.db.sql.extend.quoteTableName
import nbcp.myoql.db.sql.logQuery
import org.slf4j.LoggerFactory
import java.io.Serializable
import java.time.LocalDateTime
import kotlin.reflect.full.memberProperties

class SqlQueryClip<M : SqlBaseMetaTable<T>, T : Serializable>(var mainEntity: M) :
    SqlBaseQueryClip(mainEntity.tableName) {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    private var whereDatas = WhereData()
    val columns = mutableListOf<BaseAliasSqlSect>()
    val joins = mutableListOf<JoinTableData<*, *>>()
    val orders = mutableListOf<SqlOrderBy>()
    private val groups = mutableListOf<BaseAliasSqlSect>()
    private val having = WhereData()
    private var subSelect: SqlQueryClip<*, *>? = null //<out SqlBaseTable<out IBaseDbEntity>, out IBaseDbEntity>? = null
    private var subSelectAlias: String = ""
    private var lockType: SqlLockType? = null;

    // <0: 不出现子句， 0:nowait子句, >0: wait子句
    private var lockSeconds = 0;

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

    /**
     * 选择某些列
     */
    fun selectSome(selectColumn: (M) -> SqlColumnNames): SqlQueryClip<M, T> {
        this.columns.addAll(selectColumn(this.mainEntity))
        return this;
    }

    /**
     * 选择某列
     */
    fun select(selectColumn: (M) -> BaseAliasSqlSect): SqlQueryClip<M, T> {
        this.columns.add(selectColumn(this.mainEntity));
        return this;
    }

    fun groupBy(group: (M) -> BaseAliasSqlSect): SqlQueryClip<M, T> {
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

    /**
     * 树查询,返回结果没有单一根节点。
     */
    fun queryTree(
        pidValue: Serializable,
        idColumn: ((M) -> SqlColumnName),
        pidColumn: ((M) -> SqlColumnName)
    ): MyOqlSqlTreeData<M, T> {
        return MyOqlSqlTreeData(this, pidValue, idColumn(this.mainEntity).getAliasName(), pidColumn(this.mainEntity));
    }

    @JvmOverloads
    fun withLock(lockType: SqlLockType, lockSeconds: Int = -1): SqlQueryClip<M, T> {
        this.lockType = lockType;
        this.lockSeconds = lockSeconds;
        return this;
    }

    private fun <M2 : SqlBaseMetaTable<out T2>, T2 : Serializable> getJoinOnWhere(joinTable: M2): WhereData {
        var fks = this.mainEntity.getFks().filter { it.refTable == joinTable.tableName }
        if (fks.size == 0) {
            throw RuntimeException("找不到 ${this.mainEntity.tableName}->${joinTable.tableName} 的外键定义")
        } else if (fks.size > 1) {
            throw RuntimeException("找到多个外键定义: ${this.mainEntity.tableName}->${joinTable.tableName}")
        }

        var fk = fks.first()
        return WhereData(
            "${db.sql.getSqlQuoteName(fk.table)}.${db.sql.getSqlQuoteName(fk.column)} = ${
                db.sql.getSqlQuoteName(
                    fk.refTable
                )
            }.${db.sql.getSqlQuoteName(fk.refColumn)}"
        )
    }

    @JvmOverloads
    fun <M2 : SqlBaseMetaTable<out T2>, T2 : Serializable> join(
        joinTable: M2,
        onWhere: (M, M2) -> WhereData,
        select: ((M2) -> SqlColumnNames)? = null
    ): SqlQueryClip<M, T> {
        this.joins.add(
            JoinTableData(
                "join",
                joinTable,
                onWhere(this.mainEntity, joinTable),
                if (select == null) SqlColumnNames() else select(joinTable)
            )
        )
        return this
    }

    /**
     * 根据外键自动 onWhere
     */
    @JvmOverloads
    fun <M2 : SqlBaseMetaTable<out T2>, T2 : Serializable> join(
        joinTable: M2,
        select: ((M2) -> SqlColumnNames)? = null
    ): SqlQueryClip<M, T> {
        this.join(joinTable, { _, _ -> getJoinOnWhere(joinTable) }, select)
        return this
    }

    @JvmOverloads
    fun <M2 : SqlBaseMetaTable<out T2>, T2 : Serializable> left_join(
        joinTable: M2,
        onWhere: (M, M2) -> WhereData,
        select: ((M2) -> SqlColumnNames)? = null
    ): SqlQueryClip<M, T> {
        this.joins.add(
            JoinTableData(
                "left join", joinTable, onWhere(this.mainEntity, joinTable), select?.invoke(joinTable)
                    ?: SqlColumnNames()
            )
        )
        return this
    }

    /**
     * 根据外键自动 onWhere
     */
    @JvmOverloads
    fun <M2 : SqlBaseMetaTable<out T2>, T2 : Serializable> left_join(
        joinTable: M2,
        select: ((M2) -> SqlColumnNames)? = null
    ): SqlQueryClip<M, T> {
        this.left_join(joinTable, { _, _ -> getJoinOnWhere(joinTable) }, select)
        return this
    }


    fun orderByAsc(order: (M) -> SqlColumnName): SqlQueryClip<M, T> {
        this.orders.add(SqlOrderBy(true, SqlParameterData(order(this.mainEntity).fullName)))
        return this
    }

    fun orderByDesc(order: (M) -> SqlColumnName): SqlQueryClip<M, T> {
        this.orders.add(SqlOrderBy(false, SqlParameterData(order(this.mainEntity).fullName)))
        return this
    }

    /**
     * https://mariadb.com/kb/en/select/
     */
    override fun toSql(): SqlParameterData {
        var ret = SqlParameterData();

        if (this.subSelect != null) {
            var selectSql = this.subSelect!!.toSql();

            ret.expression += "select ";

            if (columns.any() == false) {
                if (this.subSelect!!.columns.any() == false) {
                    ret.expression += this.subSelectAlias + ".*"
                } else {
                    ret.expression += this.subSelect!!.columns.map {
                        this.subSelectAlias + "." + db.sql.getSqlQuoteName(
                            it.getAliasName()
                        )
                    }.joinToString(",")
                }
            } else {
                var selectColumn = columns.map { this.subSelectAlias + "." + db.sql.getSqlQuoteName(it.getAliasName()) }
                    .joinToString(",")

                ret.expression += selectColumn
            }

            joins.forEach {
                if (it.select.any()) {
                    ret.expression += "," + it.select.map { this.subSelectAlias + "." + db.sql.getSqlQuoteName(it.getAliasName()) }
                        .joinToString(",")
                }
            }

            ret.expression += "from ("
            ret += selectSql

            ret.expression += ")" + (if (this.subSelectAlias.HasValue) " as " + db.sql.getSqlQuoteName(this.subSelectAlias) else "")

        } else {
            ret.expression += "select "

            if (this.distinct) {
                ret.expression += "distinct "
            }


            if (columns.any() == false) {
                ret.expression += this.mainEntity.quoteTableName + ".*"
            } else {
                var selectColumn = db.sql.mergeSqlData(*columns.toTypedArray())

                ret.expression += selectColumn.expression
                ret.values += selectColumn.values
            }

            joins.forEach {
                if (it.select.any()) {
                    val select_columns = db.sql.mergeSqlData(*it.select.toTypedArray())
                    ret.expression += "," + select_columns.expression
                    ret.values += select_columns.values
                }
            }

            ret.expression += " from " + mainEntity.fromTableName
        }

        joins.forEach {
            ret.expression += " ${it.joinType} ${it.joinTable.fromTableName} on ("

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

                ret += group.toSingleSqlData()
            }
        }

        if (having.hasValue) {
            ret.expression += " having "
            ret += having.toSingleData()
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

        if (skip > 0 && take >= 0) {
            ret.expression += " limit ${skip},${take}"
        } else if (take >= 0) {
            ret.expression += " limit  ${take}"
        } else if (skip > 0) {
            ret.expression += " limit  ${skip},99999"
        }

        if (this.lockType != null) {
            if (this.lockType == SqlLockType.ShareMode) {
                ret.expression += " lock in share mode"
            } else if (this.lockType == SqlLockType.Update) {
                ret.expression += " for update"
            } else {
                throw RuntimeException("不识别的SqlLockType:${this.lockType}")
            }

            if (this.lockSeconds == 0) {
                ret.expression += " nowait"
            } else if (this.lockSeconds > 0) {
                ret.expression += " wait ${this.lockSeconds}"
            }
        }

        return ret
    }

    /**
     * 忽略 skip , take
     * @param countQuery 回调可以二次处理查询
     */
    @JvmOverloads
    fun count(countQuery: ((SqlQueryClip<M, T>) -> Unit)? = null): Int {
        var query = this.CloneObject();
        query.joins.forEach {
            it.select.clear();
        }

        query.columns.clear()
        query.columns.add(SqlColumnName.of("count(*) as cou"))
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

    @JvmOverloads
    fun toList(itemFunc: ((Map<String,Any?>) -> Unit)? = null): List<T> {
        return toList(this.mainEntity.entityClass, itemFunc);
    }

    @JvmOverloads
    fun <R> toList(entityClass: Class<R>, itemFunc: ((Map<String,Any?>) -> Unit)? = null): MutableList<R> {
        var ret = toMapList().map {
            if (itemFunc != null) {
                itemFunc(it);
            }

//            return@map mapToEntity(it, { entityClass.newInstance() })
            return@map it.ConvertJson(entityClass)
        }.toMutableList()

        return ret
    }



    var kvs = listOf<StringMap>()
    override fun doQuery(sqlParameter: SqlParameterData): List<MutableMap<String, Any?>> {
        kvs =  getMatchDefaultCacheIdValue()
        for (kv in kvs) {
            var v = getFromDefaultCache(kv);
            if (v != null) return v;
        }

        return super.doQuery(sqlParameter)
    }

    override fun afterQuery(retJsons: List<MutableMap<String, Any?>>) {
        kvs.forEach { kv ->
            FromRedisCache(
                table = this.tableName,
                groupKey = kv.keys.joinToString(","),
                groupValue = kv.values.joinToString(","),
                sql = "def"
            ).onlySetToCache(retJsons)
        }

        super.afterQuery(retJsons)
    }

    /**
     * 从缓存中获取数据
     */
    private fun getFromDefaultCache(kv: StringMap): List<MutableMap<String, Any?>>? {
        return FromRedisCache(
            table = this.tableName,
            groupKey = kv.keys.joinToString(","),
            groupValue = kv.values.joinToString(","),
            sql = "def"
        )
            .onlyGetFromCache({ it.FromListJson(MutableMap::class.java) }) as List<MutableMap<String, Any?>>?
    }


    private fun getMatchDefaultCacheIdValue(): List<StringMap> {
        var def = MongoEntityCollector.sysRedisCacheDefines.get(this.tableName)
        if (def == null) {
            return listOf();
        }

        if (this.columns.any()) return listOf();
        if (this.skip > 0) return listOf();
        if (this.take > 1) return listOf();
        if (this.joins.any()) return listOf();

        if (this.whereDatas.hasOrClip()) return listOf();


        var list = mutableListOf<StringMap>();

        def.forEach { cacheColumnGroup ->
            var kv = StringMap();
            cacheColumnGroup.forEach {
                var v = this.whereDatas.findValueFromRootLevel(this.tableName + "." + it)
                if (v.isNullOrEmpty()) return@forEach
                kv.put(it, v)
            }
            if (kv.keys.size != def.size) return@forEach;
            list.add(kv);
        }

        return list;
    }

    @JvmOverloads
    fun toEntity(mapFunc: ((Map<String,Any?>) -> Unit)? = null): T? {
        return toEntity(this.mainEntity.entityClass, mapFunc)
    }

    @JvmOverloads
    fun <R : Any> toEntity(entityClass: Class<R>, mapFunc: ((Map<String,Any?>) -> Unit)? = null): R? {
        this.take = 1
        return toMapList().map {
            mapFunc?.invoke(it)
            it.ConvertJson(entityClass)
//            mapToEntity(it, { entityClass.newInstance() })
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

    fun <M2 : SqlBaseMetaTable<out Serializable>> insertInto(insertTable: M2): Int {
        db.affectRowCount = -1;
        var select = this

        if (select.columns.any() == false) {
            select.columns.addAll(select.mainEntity.getColumns())
        }

        select.columns.removeAll { it.getAliasName() == select.mainEntity.getAutoIncrementKey() }

        //校验, 必须是表的列.
        var surplusColumns =
            select.columns.map { it.getAliasName() }.minus(insertTable::class.memberProperties.map { it.name })
        if (surplusColumns.any()) {
            throw RuntimeException("插入 select 语句时,发现多余的列: ${surplusColumns.joinToString(",")}")
        }

        var exp = "insert into ${insertTable.quoteTableName} (${
            select.columns.map { "${db.sql.getSqlQuoteName(it.getAliasName())}" }.joinToString(",")
        }) ";

        var sql = select.toSql() //.toExecuteSqlAndParameters()
        sql.expression = exp + sql.expression;
//        var executeData = SqlExecuteData(exp + sql.executeSql, sql.parameterDefines)

        var error: Exception? = null;
        var n = -1;
        var startAt = LocalDateTime.now()
        try {
            n = jdbcTemplate.update(sql.expression, sql.values)
            db.executeTime = LocalDateTime.now() - startAt

//            if (n > 0) {
//                cacheService.insertSelect4BrokeCache(insertTable.tableName)
//            }
        } catch (e: Exception) {
            error = e;
            throw e;
        } finally {
            logger.logQuery(error, tableName, sql, n);
        }

        db.affectRowCount = n
        return n
    }

    @JvmOverloads
    fun toListResult(mapFunc: ((Map<String,Any?>) -> Unit)? = null): ListResult<T> {
        return toListResult(this.mainEntity.entityClass, mapFunc);
    }

    /**
     */
    @JvmOverloads
    fun <R> toListResult(entityClass: Class<R>, mapFunc: ((Map<String,Any?>) -> Unit)? = null): ListResult<R> {
        var ret = ListResult<R>()
        var data = toList(entityClass, mapFunc)

        if (config.listResultWithCount) {
            ret.total = count()
        } else if (this.skip == 0 && this.take > 0) {
            if (data.size < this.take) {
                ret.total = data.size;
            } else {
                usingScope(arrayOf(MyOqlDbScopeEnum.IgnoreExecuteTime, MyOqlDbScopeEnum.IgnoreAffectRow)) {
                    ret.total = count()
                }
            }
        }

        ret.data = data;
        return ret
    }


    fun toMapListResult(): ListResult<JsonMap> {
        return toListResult(JsonMap::class.java);
    }
}

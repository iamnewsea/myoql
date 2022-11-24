package nbcp.myoql.db.sql.component


import nbcp.base.comm.JsonMap
import nbcp.base.comm.const
import nbcp.base.extend.*
import nbcp.base.utils.CodeUtil
import nbcp.base.utils.MyUtil
import nbcp.myoql.db.BaseEntity
import nbcp.myoql.db.db
import nbcp.myoql.db.sql.base.SqlBaseMetaTable
import nbcp.myoql.db.sql.base.SqlColumnName
import nbcp.myoql.db.sql.base.SqlColumnNames
import nbcp.myoql.db.sql.base.SqlParameterData
import nbcp.myoql.db.sql.extend.quoteTableName
import nbcp.myoql.db.sql.logInsert
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils
import org.springframework.jdbc.support.GeneratedKeyHolder
import java.io.Serializable
import java.time.LocalDateTime

/**
 * Created by yuxh on 2018/7/2
 * @param mainEntity 是元数据类型。 实体类型= mainEntity.entityClass
 */
class SqlInsertClip<M : SqlBaseMetaTable<out T>, T : Serializable>(var mainEntity: M) :
    SqlBaseExecuteClip(mainEntity.tableName) {

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    private val ignore_columns = SqlColumnNames()
    val entities = mutableListOf<JsonMap>()

    //    private var transaction = false;
    private var multiBatchSize = 256;

    //回写自增Id用。
    val ori_entities = mutableListOf<T>()

    init {
        var id = this.mainEntity.getAutoIncrementKey();
        if (id.HasValue) {
            this.ignore_columns.add(this.mainEntity.getColumns().first { it.name == id })
        }
    }

    /**
     * 批量插入时， 指定是否使用事务。
     */
//    fun useTransaction(value: Boolean = true): SqlInsertClip<M, T> {
//        this.transaction = value;
//        return this;
//    }

    /**
     * 批量插入时， 指定每次插入的条数。 会分成多次任务插入。
     */
    @JvmOverloads
    fun useMultiBatch(multiBatchSize: Int = 256): SqlInsertClip<M, T> {
        this.multiBatchSize = multiBatchSize;
        return this;
    }

    fun ignoreColumns(selectColumn: (M) -> SqlColumnNames): SqlInsertClip<M, T> {
        ignore_columns.addAll(selectColumn(this.mainEntity))
        return this
    }

    /*
    过滤掉是 null 的列.
     */
    fun addEntity(entity: T): SqlInsertClip<M, T> {
        if (entity is BaseEntity) {
            if (entity.id.isEmpty()) {
                entity.id = CodeUtil.getCode()
            }
            entity.createAt = LocalDateTime.now()
            entity.updateAt = entity.createAt
        }

        this.ori_entities.add(entity)

        var ent = entity.ConvertJson(JsonMap::class.java)

        this.entities.add(ent)
        return this
    }

    /**
     * 不过滤列.
     */
    fun addAll(entity: Collection<T>): SqlInsertClip<M, T> {
        if (entity.size == 0) return this
        entity.forEach {
            addEntity(it)
        }
        return this
    }

    override fun toSql(): SqlParameterData {
        var autoIncrmentKey = this.mainEntity.getAutoIncrementKey()

        if (entities.size == 1) {
            var entity = entities.first()

            var keys = entity.keys;

            var insertColumns = getInsertColumns()
                .filter {
                    if (it.name.IsIn(keys) == false) return@filter false;

                    var v = entity.get(it.name);
                    if (v == null) {
                        return@filter false;
                    }

                    //如果是时间，且为空字符串， 则不插入
                    if (it.dbType.isDateOrTime()) {
                        if (v is String && v.isEmpty()) {
                            return@filter false;
                        }
                    }
                    return@filter true;
                }


            var exp = "insert into ${mainEntity.quoteTableName} (${
                insertColumns.map { "${db.sql.getSqlQuoteName(it.name)}" }.joinToString(",")
            }) values (${insertColumns.map { ":${it.name}" }.joinToString(",")})";

            var executeSql = SqlParameterData(exp, entity)

            return executeSql
        }

        //在 exec 时直接生成批量
        return SqlParameterData()
    }

    override fun exec(): Int {
        if (this.entities.size == 0) return 0;

        //转换
//        var tab_converter = dbr.converter.filter { it.key.tableName == this.mainEntity.tableName }
//                .mapKeys { it.key.name }
//
//        if (tab_converter.any()) {
//            this.entities.forEach { entity ->
//                entity.keys.forEach { column ->
//                    var value = entity.get(column);
//                    if (value != null && tab_converter.containsKey(column)) {
//                        entity.put(column, tab_converter.get(column)?.convert(value.toString()) ?: value)
//                    }
//                }
//            }
//        }


        var settings = db.sql.sqlEvents?.onInserting(this) ?: arrayOf()
        if (settings.any { it.second.result == false }) {
            return 0;
        }

        var n = 0;
        if (this.entities.size == 1) {
            n = insert1()
        } else if (this.multiBatchSize <= 0) {
            n = insertMany(0, this.entities.size)
        } else {
            var batchSize = this.multiBatchSize;
            var times = this.entities.size / batchSize;
            for (i in 0..(times - 1)) {
                var batch_n = insertMany(batchSize * i, batchSize);
                if (batch_n == 0) {
                    throw RuntimeException("批量插入数据失败! 批次:${i}")
                }

                n += batch_n;
            }

            n += insertMany(batchSize * times, this.entities.size % batchSize);
        }

        db.affectRowCount = n
//        if (n > 0) {
//            cacheService.insertMany4BrokeCache(this.mainEntity.tableName)
//        }
        settings.forEach {
            it.first.insert(this, it.second);
        }

        return n
    }


//    private fun doBatch_EachItem(insertColumns: List<SqlColumnName>): IntArray {
//        db.affectRowCount = -1;
//        var exp = "insert into ${mainEntity.quoteTableName} (${insertColumns.map { "${db.sql.getSqlQuoteName(it.name)}" }.joinToString(",")}) values (${insertColumns.map { "?" }.joinToString(",")})";
//
//        var startAt = LocalDateTime.now();
//
//        var error = false
//        var n = intArrayOf();
//        try {
//            n = jdbcTemplate.batchUpdate(exp, object : BatchPreparedStatementSetter {
//                override fun getBatchSize(): Int {
//                    return entities.size
//                }
//
//                override fun setValues(ps: PreparedStatement, index: Int) {
//                    var entity = entities[index]
//
//                    insertColumns.forEachIndexed { index, key ->
//                        var item = entity.get(key.name)
//                        if (key.dbType.isDateOrTime()) {
//                            if (item != null && item is String && item.isEmpty()) {
//                                item = null;
//                            }
//                        }
//                        ps.setValue(index + 1, SqlParameterData(key.dbType.javaType, item))
//                    }
//                }
//            });
//            db.executeTime = LocalDateTime.now() - startAt
//
//            db.affectRowCount = n.sum()
//        } catch (e: Exception) {
//            error = true;
//            throw e;
//        } finally {
//            logger.InfoError(error) {
//                var msg_log = mutableListOf("[insert] ${exp}")
//                if (db.debug) {
//                    msg_log.add("[参数] ${entities.map { ent -> insertColumns.map { column -> column to ent.getStringValue(column.name) }.toMap() }.ToJson()}")
//                } else {
//                    msg_log.add("[参数] ${entities.map { ent -> insertColumns.map { column -> ent.getStringValue(column.name) }.joinToString(",") }.joinToString("\t\n")}")
//                }
//                msg_log.add("[耗时] ${db.executeTime}")
//                return@InfoError msg_log.joinToString(line_break)
//            }
//        }
//
//        return n;
//    }

    private fun getInsertColumns(): List<SqlColumnName> {
        var ignore_column_names = ignore_columns.map { it.name }
        return this.mainEntity.getColumns().filterNot { it.name.IsIn(ignore_column_names) }
    }

    private fun insertMany(skip: Int, take: Int): Int {
        if (take <= 0) return 0;

        var insertColumns = getInsertColumns()

        logger.Info { "预计批量插入 ${this.mainEntity.tableName} 总数：${entities.size} 条,skip: ${skip}, take: ${take} !" }


        var result = 0;
//        if (take < 0) {
//            result = doBatch_EachItem(insertColumns).size;
//        } else {

        var list = mutableListOf<JsonMap>()
        var v_sql = mutableListOf<String>()
        entities.Skip(skip).take(take)
            .forEachIndexed { index, jsonMap ->
                var rowMap = JsonMap();
                insertColumns.forEach { column ->
                    rowMap.put("${column}", jsonMap.get(column.name))
                }


                list.add(rowMap)
                v_sql.add("(" + rowMap.keys.joinToString(",") + ")")
            }


        var executeSql = "insert into ${mainEntity.quoteTableName} (${
            insertColumns.map { "${db.sql.getSqlQuoteName(it.name)}" }.joinToString(",")
        }) values (" + insertColumns.map { ":" + it.name }.joinToString(",") + ")"

        var msg_log = mutableListOf("[sql] ${executeSql}")
        var startAt = LocalDateTime.now();

        var error: Exception? = null;
        var index = 1;
        try {
            result = jdbcTemplate.batchUpdate(executeSql, SqlParameterSourceUtils.createBatch(list)).size

            db.executeTime = LocalDateTime.now() - startAt

            msg_log.add("批量插入完成 ${result} 条!")
        } catch (e: Exception) {
            error = e
            throw e;
        } finally {
            msg_log.add("[耗时] ${db.executeTime}")

            logger.logInsert(error, tableName, { msg_log.joinToString(const.line_break) });
        }

        return result
    }

    private fun insert1(): Int {
        db.lastAutoId = 0

        var autoIncrmentKey = this.mainEntity.getAutoIncrementKey()
        var sql = toSql()
        if (sql.expression.isEmpty()) return 0;

//        var executeData = sql.toExecuteSqlAndParameters();

        var startAt = LocalDateTime.now();

        var error: Exception? = null;
        var n = 0;

        //有自增Id的情况。
        if (autoIncrmentKey.HasValue) {
            var idKey = GeneratedKeyHolder()
            try {
                n = jdbcTemplate.update(sql.expression, MapSqlParameterSource(sql.values), idKey)

                db.executeTime = LocalDateTime.now() - startAt
                db.lastAutoId = idKey.key.toInt()
            } catch (e: Exception) {
                error = e
                throw e;
            } finally {
                logger.logInsert(error, tableName, {
                    var msg_log = mutableListOf("[sql] ${sql.expression}")
                    msg_log.add("[参数] ${sql.values.ToJson()}")
                    msg_log.add("[id] ${db.lastAutoId}")
                    msg_log.add("[result] ${n}")
                    msg_log.add("[耗时] ${db.executeTime}")
                    return@logInsert msg_log.joinToString(const.line_break)
                })
//                logger.InfoError(error) {
//                    var msg_log = mutableListOf("[sql] ${executeData.executeSql}")
//                    if (config.debug) {
//                        msg_log.add("[参数] ${executeData.executeParameters.joinToString(",")}")
//                    }
//                    msg_log.add("[id] ${db.lastAutoId}")
//                    msg_log.add("[result] ${n}")
//                    msg_log.add("[耗时] ${db.executeTime}")
//                    return@InfoError msg_log.joinToString(const.line_break)
//                }
            }

            if (this.ori_entities.any()) {
                MyUtil.setPrivatePropertyValue(this.ori_entities.first(), autoIncrmentKey, db.lastAutoId)
            }
        } else {
            //没有自增Id的情况
            try {
                n = jdbcTemplate.update(sql.expression, MapSqlParameterSource(sql.values))
                db.executeTime = LocalDateTime.now() - startAt

            } catch (e: Exception) {
                error = e
                throw e;
            } finally {
                logger.logInsert(error, tableName, {
                    var msg_log = mutableListOf("[sql] ${sql.expression}")
                    msg_log.add("[参数] ${sql.values.ToJson()}")
                    msg_log.add("[result] ${n}")
                    msg_log.add("[耗时] ${db.executeTime}")

                    return@logInsert msg_log.joinToString(const.line_break)
                })
//                logger.InfoError(error) {
//                    var msg_log = mutableListOf("[sql] ${executeData.executeSql}")
//
//                    if (config.debug) {
//                        msg_log.add("[参数] ${executeData.executeParameters.joinToString(",")}")
//                    }
//                    msg_log.add("[result] ${n}")
//                    msg_log.add("[耗时] ${db.executeTime}")
//                    return@InfoError msg_log.joinToString(const.line_break)
//                }
            }
        }

        return n
    }
}
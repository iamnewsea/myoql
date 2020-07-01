package nbcp.db.sql

import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.PreparedStatementCreator
import org.springframework.jdbc.support.GeneratedKeyHolder
import nbcp.comm.*
import nbcp.db.BaseEntity

import nbcp.utils.*
import nbcp.db.db
import java.lang.RuntimeException
import java.sql.Statement
import java.time.LocalDateTime


/**
 * Created by yuxh on 2018/7/2
 */
class SqlInsertClip<M : SqlBaseMetaTable<out T>, T : ISqlDbEntity>(var mainEntity: M) : SqlBaseExecuteClip(mainEntity.tableName) {

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    private val columns = SqlColumnNames()
    private val entities = mutableListOf<JsonMap>()
    //    private var transaction = false;
    private var multiBatchSize = 256;

    //回写自增Id用。
    private val ori_entities = mutableListOf<T>()

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
    fun useMultiBatch(multiBatchSize: Int = 256): SqlInsertClip<M, T> {
        this.multiBatchSize = multiBatchSize;
        return this;
    }

    fun resetColumns(selectColumn: (M) -> SqlColumnNames): SqlInsertClip<M, T> {
        columns.clear()
        columns.addAll(selectColumn(this.mainEntity).filter { it.name != this.mainEntity.getAutoIncrementKey() })
        return this
    }

    /*
    过滤掉是 null 的列.
     */
    fun add(entity: T): SqlInsertClip<M, T> {
        if (entity is BaseEntity) {
            if (entity.id.isEmpty()) {
                entity.id = CodeUtil.getCode()
                entity.createAt = LocalDateTime.now()
            }
        }


        this.ori_entities.add(entity)

        var ent = entity.ConvertJson(JsonMap::class.java)

        //把 布尔值 改为 1,0
        entity::class.java.AllFields
                .forEach {
                    var key = it.name;
                    var value = ent.get(key);
                    if (value == null) {
                        ent.remove(key);
                        return@forEach
                    }

                    ent.set(key, proc_value(value));
                }

        this.entities.add(ent)
        return this
    }

    /**
     * 不过滤列.
     */
    fun addAll(entity: Collection<T>): SqlInsertClip<M, T> {
        if (entity.size == 0) return this
        entity.forEach {
            add(it)
        }
        return this
    }

    override fun toSql(): SingleSqlData {
        var autoIncrmentKey = this.mainEntity.getAutoIncrementKey()

        if (entities.size == 1) {
            var entity = entities.first()

            var keys = entity.keys;

            var insertColumns = this.mainEntity.getColumns().filter { it.name != autoIncrmentKey }
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

            if (columns.any()) {
                insertColumns = insertColumns.Intersect(columns, { a, b -> a.name == b.name })
            }

            var exp = "insert into ${mainEntity.quoteTableName} (${insertColumns.map { "${db.sql.getSqlQuoteName(it.name)}" }.joinToString(",")}) values (${insertColumns.map { "{${it.name}}" }.joinToString(",")})";

            var executeSql = SingleSqlData(exp, entity)

            return executeSql
        }

        //在 exec 时直接生成批量
        return SingleSqlData()
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

        var settings = db.sql.sqlEvents.onInserting(this)
        if (settings.any { it.second != null && it.second!!.result == false }) {
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
        if (n > 0) {
            cacheService.insertMany4BrokeCache(this.mainEntity.tableName)
        }
        settings.forEach {
            it.first.insert(this, it.second);
        }

        return 0
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

    private fun insertMany(skip: Int, take: Int): Int {
        if (take <= 0) return 0;

        var autoIncrmentKey = this.mainEntity.getAutoIncrementKey();

        var insertColumns = this.mainEntity.getColumns().filter { it.name != autoIncrmentKey }

        if (columns.any()) {
            insertColumns = insertColumns.Intersect(columns, { a, b -> a.name == b.name })
        }

        logger.Info { "预计批量插入 ${this.mainEntity.tableName} 总数：${entities.size} 条,skip: ${skip}, take: ${take} !" }


        var result = 0;
//        if (take < 0) {
//            result = doBatch_EachItem(insertColumns).size;
//        } else {
        var executeSql = "insert into ${mainEntity.quoteTableName} (${insertColumns.map { "${db.sql.getSqlQuoteName(it.name)}" }.joinToString(",")}) values " +

                IntRange(1, take).map each_entity@{
                    return@each_entity "(" + insertColumns.map { "?" }.joinToString(",") + ")"
                }.joinToString(",")

        var msg_log = mutableListOf("[sql] ${executeSql}")
        var startAt = LocalDateTime.now();

        var error = false;
        var index = 1;
        try {
            result = jdbcTemplate.update(PreparedStatementCreator {
                var ps = it.prepareStatement(executeSql)


                entities.Skip(skip).take(take).forEach { ent ->
                    insertColumns.forEach {
                        ps.setValue(index++, SqlParameterData(it.dbType.javaType, ent.getOrDefault(it.name, null)))
                    }
                }

                if (logger.debug) {
                    msg_log.add("[参数]\n${entities.map { ent -> insertColumns.map { column -> ent.getStringValue(column.name) }.joinToString(",") }.joinToString("\n")}")
                }
                return@PreparedStatementCreator ps
            })
            db.executeTime = LocalDateTime.now() - startAt

            msg_log.add("批量插入完成 ${result} 条!")
        } catch (e: Exception) {
            error = true
            throw e;
        } finally {
            logger.InfoError(error) {
                msg_log.add("[耗时] ${db.executeTime}")
                return@InfoError msg_log.joinToString(line_break)
            }
        }
//        }

        return result
    }

    private fun insert1(): Int {
        db.lastAutoId = 0

        var autoIncrmentKey = this.mainEntity.getAutoIncrementKey()
        var sql = toSql()
        if (sql.expression.isEmpty()) return 0;

        var executeData = sql.toExecuteSqlAndParameters();


        var startAt = LocalDateTime.now();


        var error = false;
        var n = 0;

        //有自增Id的情况。
        if (autoIncrmentKey.HasValue) {
            var idKey = GeneratedKeyHolder()
            try {
                n = jdbcTemplate.update(PreparedStatementCreator {
                    var ps = it.prepareStatement(executeData.executeSql, Statement.RETURN_GENERATED_KEYS)
                    executeData.parameters.forEachIndexed { index, item ->
                        ps.setValue(index + 1, item)
                    }

                    return@PreparedStatementCreator ps
                }, idKey)

                db.executeTime = LocalDateTime.now() - startAt
                db.lastAutoId = idKey.key.toInt()
            } catch (e: Exception) {
                error = true
                throw e;
            } finally {
                logger.InfoError(error) {
                    var msg_log = mutableListOf("[sql] ${executeData.executeSql}")
                    if (logger.debug) {
                        msg_log.add("[参数] ${executeData.executeParameters.joinToString(",")}")
                    }
                    msg_log.add("[id] ${db.lastAutoId}")
                    msg_log.add("[result] ${n}")
                    msg_log.add("[耗时] ${db.executeTime}")
                    return@InfoError msg_log.joinToString(line_break)
                }
            }

            if (this.ori_entities.any()) {
                MyUtil.setPrivatePropertyValue(this.ori_entities.first(), autoIncrmentKey, db.lastAutoId)
            }
        } else {
            //没有自增Id的情况
            try {
                n = jdbcTemplate.update(PreparedStatementCreator {
                    var ps = it.prepareStatement(executeData.executeSql)

                    executeData.parameters.forEachIndexed { index, item ->
                        ps.setValue(index + 1, item)
                    }

                    return@PreparedStatementCreator ps
                })
                db.executeTime = LocalDateTime.now() - startAt

            } catch (e: Exception) {
                error = true
                throw e;
            } finally {
                logger.InfoError(error) {
                    var msg_log = mutableListOf("[sql] ${executeData.executeSql}")

                    if (logger.debug) {
                        msg_log.add("[参数] ${executeData.executeParameters.joinToString(",")}")
                    }
                    msg_log.add("[result] ${n}")
                    msg_log.add("[耗时] ${db.executeTime}")
                    return@InfoError msg_log.joinToString(line_break)
                }
            }
        }

        return n
    }
}
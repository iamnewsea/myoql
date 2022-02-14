package nbcp.db.sql

import nbcp.comm.JsonMap
import nbcp.comm.ToJson
import nbcp.comm.getStringValue
import nbcp.comm.minus
import nbcp.db.db
import nbcp.db.mongo.MongoEntityCollector
import nbcp.db.sql.component.JsonMapRowMapper
import nbcp.utils.Md5Util
import nbcp.utils.SpringUtil
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

abstract class SqlBaseQueryClip(tableName: String) : SqlBaseClip(tableName) {
    protected var skip = 0;
    protected var take = -1;
    protected var distinct = false;

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    /**
     * 生成缓存Md5
     */
    fun toCacheKeyMd5(): String {
        var sql = this.toSql();
        return Md5Util.getBase64Md5(sql.expression + sql.values.ToJson())
    }

    fun toMapList(): MutableList<JsonMap> {
        return toMapList(toSql());
    }

    /**
     * 返回第一列值的集合。
     */
    fun toScalarList(): List<Any?> {
        var ret = mutableListOf<Any?>()
        toMapList().forEach {
            ret.add(it.values.firstOrNull())
        }
        return ret;
    }

    protected fun toMapList(sqlParameter: SqlParameterData): MutableList<JsonMap> {
        db.affectRowCount = -1

        var settings = db.sql.sqlEvents.onSelecting(this)
        if (settings.any { it.second!!.result == false }) {
            db.affectRowCount = 0;
            return mutableListOf();
        }

        var retJsons = listOf<MutableMap<String, Any?>>()

//        var cacheKey = cacheService.getCacheKey(sql)
//
//        //先从SessionCache中找。
//
//        var cacheJson = cacheService.getCacheJson(cacheKey)
//
//
//        if (cacheJson.HasValue) {
//            //logger.info("sql query from cache: " + cacheKey.toString())
//            retJsons = cacheJson.FromJsonWithDefaultValue<MutableList<MutableMap<String, Any?>>>()
//
//        }


//        var executeData = sql //.toExecuteSqlAndParameters()
//            logger.info(executeData.executeSql +"  [" + executeData.parameters.map { it.value.AsString() }.joinToString(",") +"]");
        var startAt = LocalDateTime.now();

        var error: Exception? = null;
        try {
            retJsons = doQuery(sqlParameter)
            db.executeTime = LocalDateTime.now() - startAt


            afterQuery(retJsons);
        } catch (e: Exception) {
            error = e;
            throw e;
        } finally {
            SqlLogger.logQuery(error, this.tableName, sqlParameter, retJsons)
        }


        db.affectRowCount = retJsons.size

        settings.forEach {
            it.first.select(this, it.second, retJsons);
        }

        if (retJsons.size == 0) {
            return mutableListOf();
        }

        var retJson = retJsons.first()

        if (retJson.keys.any() == false) {
            return mutableListOf()
        }

        return retJsons.map { JsonMap(it) }.toMutableList()
    }

    protected open fun afterQuery(retJsons: List<MutableMap<String, Any?>>) {

    }


    val rowMapper: JsonMapRowMapper by lazy {
        return@lazy SpringUtil.getBean()
    }

    protected open fun doQuery(
        sqlParameter: SqlParameterData
    ): List<MutableMap<String, Any?>> {

        return jdbcTemplate.query(
            sqlParameter.expression,
            sqlParameter.values,
            rowMapper
        ) as List<MutableMap<String, Any?>>
    }


    open fun toMap(): JsonMap? {
        this.take = 1;
        return toMapList().firstOrNull()
    }

    /**
     * 返回第一列的值
     */
    fun toScalar(): Any? {
        var ret = toMap()
        if (ret == null) return null;
        return ret.values.firstOrNull()
    }

    /**
     * 判断是否存在，判断第一条记录是否为空
     */
    open fun exists(): Boolean {
        var ret = toMap()
        if (ret == null) return false;
        return ret.isNotEmpty();
    }
}
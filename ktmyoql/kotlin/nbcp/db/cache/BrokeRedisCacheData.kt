package nbcp.db.cache

import nbcp.comm.*
import nbcp.db.db
import nbcp.db.redis.scanKeys
import nbcp.utils.SpringUtil
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import kotlin.reflect.KClass


data class BrokeRedisCacheData @JvmOverloads constructor(
    var table: String = "",
    /**
     * 破坏表的隔离键，如: "cityCode"
     */
    var groupKey: String,
    /**
     * 破坏表的隔离键值，如: "010"
     */
    var groupValue: String,
    /**
     * 如果 table 为空，则使用 table = tableClass.name
     */
    val tableClass: KClass<*> = Boolean::class
) {
    constructor() : this("", "", "") {
    }

    init {
        if (this.table.isEmpty() && !this.tableClass.java.IsSimpleType()) {
            this.table = this.tableClass.java.simpleName
        }
    }

    fun brokeCache() {
        val cacheBroke = this;
        logger.Important("!执行破坏缓存! ${cacheBroke.ToJson()}")

        val redisTemplate = SpringUtil.getBean<StringRedisTemplate>();

        //场景： 对表全量删除
        if (cacheBroke.groupKey.isEmpty() || cacheBroke.groupValue.isEmpty()) {
            brokeJoinTable(redisTemplate, cacheBroke.table);

            //破坏主表
            val pattern = "sc:${cacheBroke.table}/*";
            redisTemplate.scanKeys(pattern) { key ->
                redisTemplate.delete(key)
                return@scanKeys true;
            }
            return;
        }

        //场景，有隔离键
        brokeJoinTable(redisTemplate, cacheBroke.table);

        //破坏没有隔离键的（如全量查询）,没有隔离键分两种情况：
        //A 有连接表
        var pattern = "sc:${cacheBroke.table}/*/@*"
        redisTemplate.scanKeys(pattern) { key ->
            redisTemplate.delete(key)
            return@scanKeys true;
        }

        //B 没有连接表
        pattern = "sc:${cacheBroke.table}/@*"
        redisTemplate.scanKeys(pattern) { key ->
            redisTemplate.delete(key)
            return@scanKeys true;
        }

        //破坏其它维度的隔离键
        val notMatchGroup = cacheBroke.groupKey.map { "[^${it}]" }.joinToString("")
        pattern = "sc:${cacheBroke.table}/*\\?${notMatchGroup}[^=]*"
        redisTemplate.scanKeys(pattern) { key ->
            redisTemplate.delete(key)
            return@scanKeys true;
        }

        //破坏当前隔离键值。
        pattern = "sc:${cacheBroke.table}/*\\?${cacheBroke.groupKey}=${cacheBroke.groupValue}@*"
        redisTemplate.scanKeys(pattern) { key ->
            redisTemplate.delete(key)
            return@scanKeys true;
        }
    }


    private fun brokeJoinTable(redisTemplate: StringRedisTemplate, joinTableName: String) {
        var pattern = "*/${joinTableName}/*"

        redisTemplate.scanKeys(pattern) {
            redisTemplate.delete(it);
            return@scanKeys true;
        }
    }


//    class BrokeTask(val brokeCacheData: BrokeRedisCacheData) {
//        fun waitDone() {
//            brokeCacheItemNoWait(brokeCacheData)
//        }
//    }

    companion object {
        val logger = LoggerFactory.getLogger(this::class.java.declaringClass)

        fun of(cacheForBroke: BrokeRedisCache, variableMap: JsonMap): BrokeRedisCacheData {
            val spelExecutor = CacheKeySpelExecutor(variableMap);
            val ret = BrokeRedisCacheData();

            ret.table = spelExecutor.getVariableValue(cacheForBroke.getTableName());
            ret.groupKey = spelExecutor.getVariableValue(cacheForBroke.groupKey);
            ret.groupValue = spelExecutor.getVariableValue(cacheForBroke.groupValue);
            return ret;
        }

    }
}

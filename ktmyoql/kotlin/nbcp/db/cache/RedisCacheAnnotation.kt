package nbcp.db.cache

import nbcp.comm.*
import nbcp.db.redis.RedisTask
import nbcp.utils.Md5Util
import nbcp.utils.SpringUtil
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext
import java.lang.annotation.Inherited
import java.time.Duration
import java.util.function.Supplier

/**
 * Sql Select Cache
 */
@Inherited
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CacheForSelect(
    val cacheSeconds: Int,
    /**
     * 缓存表
     */
    val table: String,
    /**
     * 缓存关联表
     */
    val joinTables: Array<String>,
    /**
     * 缓存表的隔离键或主键, 如:"cityCode"
     */
    val key: String = "",
    /**
     * 缓存表的隔离值,如: "010"
     */
    val value: String = "",

//    val sql: String = ""
) {
}


/**
 * Sql Update/Insert/Delete Cache
 */
@Inherited
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CacheForBroke(
    /**
     * 破坏表
     */
    val table: String,
    /**
     * 破坏表的隔离键，如: "cityCode"
     */
    val key: String = "",
    /**
     * 破坏表的隔离键值，如: "010"
     */
    val value: String = ""
)


//-----------------------

/**
 * CacheForSelect 的数据类
 */
data class CacheForSelectData(
    var cacheSeconds: Int = 0,
    /**
     * 缓存表
     */
    var table: String = "",
    /**
     * 缓存关联表
     */
    var joinTables: Array<String> = arrayOf(),
    /**
     * 缓存表的隔离键, 如:"cityCode"
     */
    var key: String = "",
    /**
     * 缓存表的隔离值,如: "010"
     */
    var value: String = "",
    var sql: String = ""
) {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)

        fun of(cacheForSelect: CacheForSelect, sql: String, variableMap: JsonMap): CacheForSelectData {
            var spelExecutor = CacheSpelExecutor(variableMap);
            var ret = CacheForSelectData();
            ret.cacheSeconds = cacheForSelect.cacheSeconds;
            ret.table = spelExecutor.getVariableValue(cacheForSelect.table);
            ret.joinTables = cacheForSelect.joinTables;
            ret.key = spelExecutor.getVariableValue(cacheForSelect.key);
            ret.value = spelExecutor.getVariableValue(cacheForSelect.value);
            ret.sql = spelExecutor.getVariableValue(sql);
            return ret
        }


        /**
         * 缓存数据源，使用系统固定的数据库，不涉及分组及上下文切换。
         */
        private val redisTemplate by lazy {
            return@lazy SpringUtil.getBean<StringRedisTemplate>()
        }
    }


    /**
     * sc=sqlcache
     * key规则：5部分： sc:{主表}:{join_tables.sort().join(":")}:{主表key}@{key_value}:{sql/md5}
     * 如： sc:主表:|join_tab1|join_tab2|:cityCode-010:select*from主表wherecityCode=010anddeleted!=0
     * 主表规则：  sc:表:*
     * join表规则:  sc:*|表|* , join表为空，没有|
     * 主表key:   sc:*:表key@*
     * 表主value:  sc:*@表value:*
     *
     * 约束:  每个部分不能出现半角 冒号，竖线，@,出现部分用全角代替
     *
     * scan:   sc:table1:*:table1_column1-value1:*
     */
    fun getCacheKey(): String {
        var cache = this;
        var ret = mutableListOf<String>();
        ret.add("sc")
        ret.add(cache.table);

        if (cache.joinTables.any()) {
            ret.add("|" + cache.joinTables.toSortedSet().joinToString("|") + "|")
        } else {
            ret.add("")
        }

        if (cache.key.HasValue && cache.value.HasValue) {
            ret.add("${cache.key}@${cache.value}")
        }

        var ext = cache.sql

        if (ext.length > 32) {
            ret.add(Md5Util.getBase64Md5(ext))
        } else {
            ret.add(ext)
        }
        return ret.joinToString(":")
    }


    fun <T> usingRedisCache(clazz: Class<T>, consumer: Supplier<Any>): T {
        var cacheKey = this.getCacheKey()

        if (this.cacheSeconds >= 0) {
            var redisTemplate = SpringUtil.getBean<StringRedisTemplate>();
            var cacheValue = redisTemplate.opsForValue().get(cacheKey).AsString()
            if (cacheValue.HasValue) {
                logger.warn("从Redis缓存加载数据:${this.ToJson()}")
                return cacheValue.FromJson(clazz)!!
            }
        }

        var ret = consumer.get();

        if (ret != null) {
            var cacheSeconds = this.cacheSeconds
            //默认3分钟
            if (cacheSeconds == 0) {
                cacheSeconds = config.getConfig("app.cache.${this.table}").AsInt(180);
            }

            if (cacheSeconds > 0) {
                redisTemplate.opsForValue().set(cacheKey, ret.ToJson(), Duration.ofSeconds(cacheSeconds.toLong()));
            }
        }
        return ret as T;
    }
}


data class CacheForBrokeData(
    var table: String = "",
    /**
     * 破坏表的隔离键，如: "cityCode"
     */
    var key: String = "",
    /**
     * 破坏表的隔离键值，如: "010"
     */
    var value: String = ""
) {
    fun brokeCache() {
        RedisTask.setDelayBrokeCacheKey(this)
    }

    companion object {
        fun of(cacheForBroke: CacheForBroke, variableMap: JsonMap): CacheForBrokeData {
            var spelExecutor = CacheSpelExecutor(variableMap);
            var ret = CacheForBrokeData();
            ret.table = spelExecutor.getVariableValue(cacheForBroke.table);
            ret.key = spelExecutor.getVariableValue(cacheForBroke.key);
            ret.value = spelExecutor.getVariableValue(cacheForBroke.value);
            return ret;
        }
    }
}

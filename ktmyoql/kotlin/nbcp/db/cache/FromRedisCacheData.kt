package nbcp.db.cache

import nbcp.comm.*
import nbcp.utils.Md5Util
import nbcp.utils.SpringUtil
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import java.time.Duration
import java.util.function.Consumer
import java.util.function.Supplier
import kotlin.reflect.KClass

/**
 * CacheForSelect 的数据类
 */

data class FromRedisCacheData @JvmOverloads constructor(

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
    var groupKey: String,
    /**
     * 缓存表的隔离值,如: "010"
     */
    var groupValue: String,

    /**
     * 唯一值
     */
    var sql: String,
    var cacheSeconds: Int = 3600,
    /**
     * 如果 table 为空，则使用 table = tableClass.name
     */
    val tableClass: KClass<*> = Boolean::class
) {
    init {
        if (this.table.isEmpty() && !this.tableClass.java.IsSimpleType()) {
            this.table = this.tableClass.java.simpleName
        }
    }

    constructor() : this("", arrayOf(), "", "", "") {

    }

    companion object {
        val logger = LoggerFactory.getLogger(this::class.java.declaringClass)

        fun of(cacheForSelect: FromRedisCache, sql: String, variableMap: JsonMap): FromRedisCacheData {
            val spelExecutor = CacheKeySpelExecutor(variableMap);
            val ret = FromRedisCacheData();
            ret.cacheSeconds = cacheForSelect.cacheSeconds;

            ret.table = spelExecutor.getVariableValue(cacheForSelect.getTableName());
            ret.joinTables = cacheForSelect.joinTables;
            ret.groupKey = spelExecutor.getVariableValue(cacheForSelect.groupKey);
            ret.groupValue = spelExecutor.getVariableValue(cacheForSelect.groupValue);
            ret.sql = spelExecutor.getVariableValue(cacheForSelect.sql.AsString(sql));
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
     * sc:查询主表/连接表/排序/?查询主表隔离键=查询主表隔离键值@md5
     */
    fun getCacheKey(): String {
        val cache = this;
        val ret = mutableListOf<String>();
        ret.add("sc:")
        ret.add(cache.table);
        ret.add("/")

        if (cache.joinTables.any()) {
            ret.add(
                cache.joinTables.toSortedSet().map { it + "/" }.joinToString("")
            )
        }

        if (cache.groupKey.HasValue && cache.groupValue.HasValue) {
            ret.add("?${cache.groupKey}=${cache.groupValue}")
        }

        ret.add("@")
        ret.add(Md5Util.getBase64Md5(cache.sql))

        return ret.joinToString("")
    }

    fun <T> usingRedisCache(cacheType: Class<out T>, consumer: Supplier<T>): T {
        return usingRedisCache({ it.FromJson(cacheType)!! }, consumer);
    }

    fun <T> usingRedisCacheForList(cacheType: Class<out T>, consumer: Supplier<List<T>>): List<T> {
        return usingRedisCache({ it.FromListJson(cacheType) }, consumer);
    }

    private fun <T> usingRedisCache(cacheString: java.util.function.Function<String, T>, consumer: Supplier<T>): T {

        val cacheKey = this.getCacheKey()

        if (this.cacheSeconds >= 0 && cacheKey.HasValue) {
            val redisTemplate = SpringUtil.getBean<StringRedisTemplate>();
            val cacheValue = redisTemplate.opsForValue().get(cacheKey).AsString()
            if (cacheValue.HasValue) {
                logger.Important("!查到Redis缓存数据! cacheKey:${cacheKey},sql:${this.sql}")
//                if (clazz == Any::class.java || clazz == java.lang.Object::class.java) {
//                    return cacheValue as T;
//                }
//                return cacheValue.FromJson(clazz)!!
                return cacheString.apply(cacheValue)
            }
        }

        val ret = consumer.get();

        if (cacheSeconds >= 0 && cacheKey.HasValue) {
            var cacheSeconds = this.cacheSeconds
            //默认3分钟
            if (cacheSeconds == 0) {
                cacheSeconds = config.getConfig("app.cache.${this.table}").AsInt(180);
            }

            if (cacheSeconds > 0) {
                redisTemplate.opsForValue().set(cacheKey, ret.ToJson(), Duration.ofSeconds(cacheSeconds.toLong()));
            }
        }
        return ret;
    }
}

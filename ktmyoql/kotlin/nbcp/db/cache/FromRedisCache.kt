package nbcp.db.cache

import nbcp.comm.*
import nbcp.utils.Md5Util
import nbcp.utils.SpringUtil
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import java.lang.annotation.Inherited
import java.time.Duration
import java.util.function.Supplier
import kotlin.reflect.KClass

/**
 * Sql Select Cache
 */
@Inherited
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class FromRedisCache constructor(
    /**
     * 如果 table 为空，则使用 table = tableClass.name
     */
    val tableClass: KClass<*> = Void::class,

    /**
     * 缓存关联表
     */
    val joinTableClasses: Array<KClass<*>> = arrayOf(),

    /**
     * 缓存表的隔离键或主键, 如:"cityCode"
     */
    val groupKey: String = "",
    /**
     * 缓存表的隔离值,如: "010",如果使用参数变量,使用 # 开头
     */
    val groupValue: String = "",

    /**
     * 唯一值
     */
    val sql: String = "",

    val cacheSeconds: Int = 3600,
    /**
     * 缓存表
     */
    val table: String = "",
    /**
     * 缓存关联表
     */
    val joinTables: Array<String> = arrayOf(),
) {
    companion object {
        internal val logger = LoggerFactory.getLogger(FromRedisCache::class.java)
    }
}


fun FromRedisCache.getTableName(): String {
    if (this.table.HasValue) return this.table;

    if (this.tableClass == Void::class) {
        throw RuntimeException("需要指定 主表!")
    }

    return this.tableClass.java.simpleName;
}

fun FromRedisCache.getJoinTableNames(): Array<String> {
    var joinTables = this.joinTables;
    if (joinTables.any()) {
        return joinTables;
    }

    return this.joinTableClasses.map { it.simpleName!! }.toTypedArray()
}

/**
 * 解析变量
 */
fun FromRedisCache.resolveWithVariable(variableMap: Map<String, Any?>, sql: String = ""): FromRedisCache {
    val spelExecutor = CacheKeySpelExecutor(variableMap);
    return FromRedisCache(
        Void::class,
        arrayOf(),
        spelExecutor.getVariableValue(this.groupKey),
        spelExecutor.getVariableValue(this.groupValue),
        spelExecutor.getVariableValue(sql.AsString(this.sql)),
        this.cacheSeconds,
        spelExecutor.getVariableValue(this.getTableName()),
        this.getJoinTableNames()
    )
}


/**
 * 缓存数据源，使用系统固定的数据库，不涉及分组及上下文切换。
 */
private val redisTemplate by lazy {
    return@lazy SpringUtil.getBean<StringRedisTemplate>()
}

/**
 * sc=sqlcache
 * sc:查询主表/连接表1/连接表2/连接表3/?查询主表隔离键=查询主表隔离键值@md5
 */
fun FromRedisCache.getCacheKey(): String {
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


inline fun <reified T> FromRedisCache.getJson(consumer: Supplier<T?>): T? {
    return this.getJson(T::class.java, consumer);
}

fun <T> FromRedisCache.getJson(cacheType: Class<out T>, consumer: Supplier<out T?>): T? {
    return usingRedisCache({ it.FromJson(cacheType) }, consumer);
}

fun <T> FromRedisCache.getList(cacheType: Class<out T>, consumer: Supplier<List<T>?>): List<T>? {
    return usingRedisCache({ it.FromListJson(cacheType) }, consumer)
}

fun <T> FromRedisCache.onlyGetFromCache(converter: java.util.function.Function<String, T?>): T? {
    val cacheKey = this.getCacheKey()

    if (this.cacheSeconds >= 0 && cacheKey.HasValue) {
        val redisTemplate = SpringUtil.getBean<StringRedisTemplate>();
        val cacheValue = redisTemplate.opsForValue().get(cacheKey).AsString()
        if (cacheValue.HasValue) {
            var ret = converter.apply(cacheValue);
            if (ret != null) {
                FromRedisCache.logger.Important("!查到Redis缓存数据! cacheKey:${cacheKey},sql:${this.sql}")
                return ret;
            }
        }
    }
    return null;
}

fun FromRedisCache.onlySetToCache(ret: Any) {
    val cacheKey = this.getCacheKey()

    if (cacheSeconds >= 0 && cacheKey.HasValue) {
        var cacheSeconds = this.cacheSeconds
        //默认3分钟
        if (cacheSeconds == 0) {
            cacheSeconds = config.getConfig("app.cache.${this.table}").AsInt(180);
        }

        if (cacheSeconds > 0) {
            redisTemplate.opsForValue()
                .set(cacheKey, ret.ToJson(), Duration.ofSeconds(cacheSeconds.toLong()));
        }
    }
}

private fun <T> FromRedisCache.usingRedisCache(
    converter: java.util.function.Function<String, T?>,
    consumer: Supplier<out T?>
): T? {
    onlyGetFromCache(converter).apply {
        if (this != null) {
            return this;
        }
    }

    val ret = consumer.get();
    if (ret == null) return null;

    onlySetToCache(ret)
    return ret;
}


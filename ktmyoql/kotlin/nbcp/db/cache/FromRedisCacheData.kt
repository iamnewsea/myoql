package nbcp.db.cache

import nbcp.comm.*
import nbcp.utils.Md5Util
import nbcp.utils.SpringUtil
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import java.time.Duration
import java.util.function.Supplier

/**
 * CacheForSelect 的数据类
 */
data class FromRedisCacheData(
    var cacheSeconds: Int,
    /**
     * 缓存表
     */
    var table: String,
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
    var sql: String
) {
    constructor() : this(0, "", arrayOf(), "", "", "") {

    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)

        fun of(cacheForSelect: FromRedisCache, sql: String, variableMap: JsonMap): FromRedisCacheData {
            var spelExecutor = CacheKeySpelExecutor(variableMap);
            var ret = FromRedisCacheData();
            ret.cacheSeconds = cacheForSelect.cacheSeconds;
            ret.table = spelExecutor.getVariableValue(cacheForSelect.table);
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

        const val SQL_CACHE_PREFIX = "sc"
        const val GROUP_JOIN_CHAR = ':'
        const val JOIN_TABLE_CHAR = '|';
        const val KEY_VALUE_JOIN_CHAR = '~';
        const val TAIL_CHAR = '@';
    }


    /**
     * sc=sqlcache
     * key规则：4部分：(冒号分隔的部分)
     *  1. sc标志
     *  2. {主表}
     *  3. |{join_tables.sort().join("|")}| 或 空字符串
     *  4. {主表key}~{key_value}@{sql/md5}
     *
     * 如： sc:主表:|join_tab1|join_tab2|:cityCode~010@select*from主表wherecityCode=010anddeleted!=0
     * 查主表规则：  sc:表:*
     * 查join表规则:  sc:*|表|* , join表为空，没有|
     * 查主表key:   sc:*:表key#*
     * 查主表value:  sc:*~表value@*
     *
     * 约束:  每个部分不能出现半角 冒号，竖线，~, @,出现部分用全角代替
     *
     * scan:   sc:table1:*:table1~column1#value1@*
     */
    fun getCacheKey(): String {
        var cache = this;
        var ret = mutableListOf<String>();
        ret.add(SQL_CACHE_PREFIX)
        ret.add(cache.table);

        if (cache.joinTables.any()) {
            ret.add(
                JOIN_TABLE_CHAR + cache.joinTables.toSortedSet()
                    .joinToString(JOIN_TABLE_CHAR.toString()) + JOIN_TABLE_CHAR
            )
        } else {
            ret.add("")
        }


        var part2 = mutableListOf<String>()
        if (cache.groupKey.HasValue && cache.groupValue.HasValue) {
            part2.add("${cache.groupKey}${KEY_VALUE_JOIN_CHAR}${cache.groupValue}")
        } else {
            part2.add("")
        }

        part2.add(TAIL_CHAR.toString())

        var ext = cache.sql
        if (ext.length > 32) {
            part2.add(Md5Util.getBase64Md5(ext))
        } else {
            part2.add(ext)
        }

        ret.add(part2.joinToString(""))

        return ret.joinToString(GROUP_JOIN_CHAR.toString())
    }


    fun <T> usingRedisCache(clazz: Class<T>, consumer: Supplier<Any>): T {
        var cacheKey = this.getCacheKey()

        if (this.cacheSeconds >= 0 && cacheKey.HasValue) {
            var redisTemplate = SpringUtil.getBean<StringRedisTemplate>();
            var cacheValue = redisTemplate.opsForValue().get(cacheKey).AsString()
            if (cacheValue.HasValue) {
                logger.warn("从Redis缓存加载数据:${this.ToJson()}")
                return cacheValue.FromJson(clazz)!!
            }
        }

        var ret = consumer.get();

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
        return ret as T;
    }
}

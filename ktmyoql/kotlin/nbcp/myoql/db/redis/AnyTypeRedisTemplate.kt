package nbcp.myoql.db.redis

import nbcp.base.comm.config
import nbcp.base.extend.AsLong
import nbcp.base.extend.HasValue
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ScanOptions


/**
 * 使用 scan 替代 keys,仅限于当前产品线
 */
@JvmOverloads
fun RedisTemplate<*, *>.scanKeys(pattern: String, limit: Int = 9999, callback: (String) -> Boolean) {
    var prefix = "";
    if (config.redisProductLineCodePrefixEnable) {
        prefix = config.appPrefix;
    }

    var searchPatternValue = pattern;
    if (prefix.HasValue &&
            !searchPatternValue.startsWith(prefix + ":")
    ) {
        searchPatternValue = prefix + ":" + searchPatternValue
    }

    this.scanAllKeys(searchPatternValue, limit, callback)
}

/**
 * 不受产品线前缀的影响
 * 不要用 Jedis，因为它不支持集群。
 * 使用默认的： org.springframework.boot:spring-boot-starter-data-redis ,支持集群模式扫描！
 */
@JvmOverloads
fun RedisTemplate<*, *>.scanAllKeys(pattern: String, limit: Int = 9999, callback: (String) -> Boolean) {
    this.connectionFactory
            .connection
            .use { conn ->
                conn.scan(
                        ScanOptions
                                .scanOptions()
                                .match(pattern)
                                .count(limit.AsLong())
                                .build()
                ).use { result ->
                    while (result.hasNext()) {
                        val key = String(result.next())
                        if (callback(key) == false) {
                            break;
                        }
                    }
                }
            }
}
package nbcp.db.redis

import nbcp.comm.AsLong
import nbcp.comm.HasValue
import nbcp.comm.config
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ScanOptions


/**
 * 使用 scan 替代 keys,仅限于当前产品线
 */
@JvmOverloads
fun RedisTemplate<*, *>.scanKeys(pattern: String, limit: Int = 9999, callback: (String) -> Boolean) {
    var searchPatternValue = pattern;
    if (config.productLineCode.HasValue &&
        !searchPatternValue.startsWith(config.productLineCode + ":")
    ) {
        searchPatternValue = config.productLineCode + ":" + searchPatternValue
    }

    this.scanAllKeys(searchPatternValue, limit, callback)
}

/**
 * 不受产品线前缀的影响
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
package nbcp.myoql.db.redis

import nbcp.base.comm.*;
import nbcp.base.db.*;
import nbcp.base.enums.*;
import nbcp.base.extend.*;
import nbcp.base.utils.*;
import nbcp.myoql.db.enums.*
import nbcp.myoql.db.*;
import nbcp.myoql.db.comm.*
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ScanOptions


/**
 * 使用 scan 替代 keys,仅限于当前产品线
 */
@JvmOverloads
fun RedisTemplate<*, *>.scanKeys(pattern: String, limit: Int = 9999, callback: (String) -> Boolean) {
    val prefix = config.appPrefix;

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
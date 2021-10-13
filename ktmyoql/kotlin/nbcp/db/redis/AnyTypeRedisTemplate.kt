package nbcp.db.redis

import nbcp.comm.AsLong
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ScanOptions


/**
 * 使用 scan 替代 keys
 */
@JvmOverloads
fun RedisTemplate<*, *>.scanKeys(pattern: String, limit: Int = 9999, callback: (String) -> Boolean) {
    var list = mutableSetOf<String>()

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
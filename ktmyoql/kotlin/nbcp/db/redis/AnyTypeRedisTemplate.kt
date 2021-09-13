package nbcp.db.redis

import nbcp.comm.AsLong
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ScanOptions


/**
 * 使用 scan 替代 keys
 */
fun RedisTemplate<*, *>.scanKeys(pattern: String, limit: Int = 999): Set<String> {
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
                    var item = result.next();
                    list.add(String(item))
                }
            }
        }
    return list;
}
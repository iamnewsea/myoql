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
        .clusterConnection
        .use { conn ->
            conn.scan(
                ScanOptions
                    .scanOptions()
                    .match(pattern)
                    .count(limit.AsLong())
                    .build()
            ).use { result ->
                while (result.hasNext()) {
                    list.add(result.next().toString())
                }
            }
        }
    return list;
}
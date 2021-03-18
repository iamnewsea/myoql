package nbcp.db.redis

import nbcp.comm.AsLong
import nbcp.comm.HasValue
import nbcp.db.cache.CacheForBroke
import nbcp.db.cache.RedisCacheIntercepter
import nbcp.model.MasterAlternateStack
import nbcp.utils.MyUtil
import nbcp.utils.SpringUtil
import org.springframework.data.redis.connection.DefaultStringRedisConnection
import org.springframework.data.redis.connection.RedisConnection
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ScanOptions
import org.springframework.data.redis.core.SetOperations
import org.springframework.data.redis.serializer.RedisSerializer
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import kotlin.concurrent.thread

//class AnyTypeRedisTemplate() : RedisTemplate<String, Any>() {
//    init {
//        //使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值
////        var serializer = Jackson2JsonRedisSerializer(Any::class.java);
////        serializer.setObjectMapper(FieldTypeJsonMapper.instance);
//
//        this.keySerializer = RedisSerializer.string()
//        this.valueSerializer = RedisSerializer.string()
//        this.hashKeySerializer = RedisSerializer.string()
//        this.hashValueSerializer = RedisSerializer.json()
//    }
//
//    constructor(connectionFactory: RedisConnectionFactory) : this() {
//        this.connectionFactory = connectionFactory
//        this.afterPropertiesSet()
//    }
//
//    override fun preProcessConnection(connection: RedisConnection, existingConnection: Boolean): RedisConnection {
//        return DefaultStringRedisConnection(connection)
//    }
//
//    override fun opsForSet(): SetOperations<String, Any> {
//        return super.opsForSet()
//    }
//}

/**
 * 使用 scan 替代 keys
 */
fun RedisTemplate<*,*>.scanKeys(pattern: String, limit: Int = 999): Set<String> {
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
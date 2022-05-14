package nbcp.db.redis.proxy

import nbcp.comm.*
import nbcp.utils.*
import nbcp.db.redis.BaseRedisProxy
import java.time.Duration

/**
 * Created by udi on 17-7-14.
 */
class RedisJsonProxy<T> @JvmOverloads constructor(
        group: String,
        val clazz:Class<T>,
        defaultCacheSeconds: Int = 0) :
        BaseRedisProxy(group, defaultCacheSeconds) {


    fun get(key: String = ""): T? {
        var cacheKey = getFullKey(key)
        var value = stringCommand.opsForValue().get(cacheKey)
        if (value == null) return null;
        return value.FromJson(clazz)
    }

//    fun setKey(value: String, cacheSecond: Int = defaultCacheSeconds) = setKey("", value, cacheSecond);

    fun set(key: String, value: T, cacheSecond: Int = defaultCacheSeconds) {
        var cacheKey = getFullKey(key)

        if (cacheSecond <= 0) {
            stringCommand.opsForValue().set(cacheKey, value.ToJson())
        } else {
            stringCommand.opsForValue().set(cacheKey, value.ToJson(), Duration.ofSeconds(cacheSecond.AsLong()))
        }
    }
}



package nbcp.db.redis.proxy

import nbcp.comm.*
import nbcp.utils.*
import nbcp.db.redis.BaseRedisProxy
import java.time.Duration

/**
 * Created by udi on 17-7-14.
 */
class RedisJsonProxy<T> @JvmOverloads constructor(
    key: String,
    val clazz: Class<T>,
    defaultCacheSeconds: Int = 0,
    val autoRenewal: Boolean = false
) :
    BaseRedisProxy(key, defaultCacheSeconds) {


    fun get( ): T? {
        var cacheKey = getFullKey(key)
        var value = stringCommand.opsForValue().get(cacheKey)
        if (value == null) return null;

        if (autoRenewal) {
            renewalKey( )
        }
        return value.FromJson(clazz)
    }

//    fun setKey(value: String, cacheSecond: Int = defaultCacheSeconds) = setKey("", value, cacheSecond);

    fun set(  value: T, cacheSecond: Int = defaultCacheSeconds) {
        var cacheKey = getFullKey(key)

        this.defaultCacheSeconds = cacheSecond;

        if (cacheSecond <= 0) {
            stringCommand.opsForValue().set(cacheKey, value.ToJson())
        } else {
            stringCommand.opsForValue().set(cacheKey, value.ToJson(), Duration.ofSeconds(cacheSecond.AsLong()))
        }
    }
}



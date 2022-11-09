package nbcp.myoql.db.redis.proxy

import nbcp.base.extend.AsLong
import nbcp.base.extend.FromJson
import nbcp.base.extend.ToJson
import nbcp.myoql.db.redis.BaseRedisProxy
import java.time.Duration

/**
 * Created by udi on 17-7-14.
 */
class RedisJsonProxy<T> @JvmOverloads constructor(
    key: String,
    val clazz: Class<T>,
    defaultCacheSeconds: Int = 0,
    autoRenewal: Boolean = false
) :
    BaseRedisProxy(key, defaultCacheSeconds, autoRenewal) {


    fun get(): T? {
        var cacheKey = getFullKey(key)
        var value = stringCommand.opsForValue().get(cacheKey)
        if (value == null) return null;

        if (autoRenewal) {
            renewalKey()
        }
        return value.FromJson(clazz)
    }

//    fun setKey(value: String, cacheSecond: Int = defaultCacheSeconds) = setKey("", value, cacheSecond);

    @JvmOverloads
    fun set(value: T) {
        var cacheKey = getFullKey(key)

        if (this.defaultCacheSeconds <= 0) {
            stringCommand.opsForValue().set(cacheKey, value.ToJson())
        } else {
            stringCommand.opsForValue().set(cacheKey, value.ToJson(), Duration.ofSeconds(this.defaultCacheSeconds.AsLong()))
        }
    }
}



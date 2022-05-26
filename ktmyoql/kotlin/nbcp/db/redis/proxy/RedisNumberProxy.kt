package nbcp.db.redis.proxy

import nbcp.comm.*
import nbcp.utils.*
import nbcp.db.redis.BaseRedisProxy
import java.time.Duration


class RedisNumberProxy(
    group: String,
    defaultCacheSeconds: Int = 0,
    val autoRenewal: Boolean = false
) :
    BaseRedisProxy(group, defaultCacheSeconds) {

    fun get(key: String = ""): Long {
        var cacheKey = getFullKey(key)
        var value = stringCommand.opsForValue().get(cacheKey).AsLong()

        if (autoRenewal) {
            renewalKey(key)
        }
        return value.AsLong()
    }

//    fun set(value: Long, cacheSecond: Int = defaultCacheSeconds) = setKey("",value,cacheSecond)

    /**
     * @param cacheSecond: 0=默认值 , -1为不设置缓存时间
     */
    fun set(key: String, value: Long, cacheSecond: Int = defaultCacheSeconds) {
        var cacheKey = getFullKey(key)
        this.defaultCacheSeconds = cacheSecond;

        if (cacheSecond <= 0) {
            stringCommand.opsForValue().set(cacheKey, value.toString())
        } else {
            stringCommand.opsForValue().set(cacheKey, value.toString(), Duration.ofSeconds(cacheSecond.AsLong()))
        }
    }


    fun increment(key: String, value: Int = 1): Long {
        var cacheKey = getFullKey(key)
        if (cacheKey.isEmpty()) return -1L

        return stringCommand.opsForValue().increment(cacheKey, value.AsLong())
    }

    fun decrement(key: String, value: Int = 1): Long {
        var cacheKey = getFullKey(key)
        if (cacheKey.isEmpty()) return -1L

        return stringCommand.opsForValue().decrement(cacheKey, value.AsLong())
    }
}



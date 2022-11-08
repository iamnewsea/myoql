package nbcp.myoql.db.redis.proxy

import nbcp.base.comm.*
import nbcp.base.extend.AsLong
import nbcp.base.utils.*
import nbcp.myoql.db.redis.BaseRedisProxy
import java.time.Duration


class RedisNumberProxy @JvmOverloads constructor(
    key: String,
    defaultCacheSeconds: Int = 0,
    autoRenewal: Boolean = false
) :
    BaseRedisProxy(key, defaultCacheSeconds,autoRenewal) {

    fun get(): Long {
        var cacheKey = getFullKey(key)
        var value = stringCommand.opsForValue().get(cacheKey).AsLong()

        if (autoRenewal) {
            renewalKey()
        }
        return value.AsLong()
    }

//    fun set(value: Long, cacheSecond: Int = defaultCacheSeconds) = setKey("",value,cacheSecond)

    /**
     */
    @JvmOverloads
    fun set(value: Long) {
        var cacheKey = getFullKey(key)

        if (this.defaultCacheSeconds <= 0) {
            stringCommand.opsForValue().set(cacheKey, value.toString())
        } else {
            stringCommand.opsForValue().set(cacheKey, value.toString(), Duration.ofSeconds(this.defaultCacheSeconds.AsLong()))
        }
    }


    @JvmOverloads
    fun increment(value: Int = 1): Long {
        var cacheKey = getFullKey(key)
        if (cacheKey.isEmpty()) return -1L

        if(autoRenewal){
            renewalKey()
        }
        return stringCommand.opsForValue().increment(cacheKey, value.AsLong())
    }

    @JvmOverloads
    fun decrement(value: Int = 1): Long {
        var cacheKey = getFullKey(key)
        if (cacheKey.isEmpty()) return -1L

        if(autoRenewal){
            renewalKey()
        }
        return stringCommand.opsForValue().decrement(cacheKey, value.AsLong())
    }
}



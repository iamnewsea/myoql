package nbcp.db.redis.proxy

import nbcp.comm.*
import nbcp.utils.*
import nbcp.db.redis.BaseRedisProxy
import nbcp.db.redis.RedisRenewalTypeEnum
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.time.Duration

/**
 * Created by udi on 17-7-14.
 */
class RedisStringProxy @JvmOverloads constructor(
        group: String,
        defaultCacheSeconds: Int = 0) :
        BaseRedisProxy(group, defaultCacheSeconds) {


    fun get(key: String = ""): String {
        var cacheKey = getFullKey(key)
        var value = stringCommand.opsForValue().get(cacheKey)
        if (value == null) return "";
        return value
    }

//    fun setKey(value: String, cacheSecond: Int = defaultCacheSeconds) = setKey("", value, cacheSecond);

    fun set(key: String, value: String, cacheSecond: Int = defaultCacheSeconds) {
        var cacheKey = getFullKey(key)

        if (cacheSecond <= 0) {
            stringCommand.opsForValue().set(cacheKey, value)
        } else {
            stringCommand.opsForValue().set(cacheKey, value, Duration.ofSeconds(cacheSecond.AsLong()))
        }
    }

//    fun append(key:String,value:String){
//        var cacheKey = getFullKey(key)
//
//        stringCommand.opsForValue().append(cacheKey, value)
//    }
}


class RedisNumberProxy(
        group: String,
        defaultCacheSeconds: Int = 0) :
        BaseRedisProxy(group, defaultCacheSeconds) {

    fun get(key: String = ""): Long {
        var cacheKey = getFullKey(key)
        var value = stringCommand.opsForValue().get(cacheKey).AsLong()
        return value.AsLong()
    }

//    fun set(value: Long, cacheSecond: Int = defaultCacheSeconds) = setKey("",value,cacheSecond)

    fun set(key: String, value: Long, cacheSecond: Int = defaultCacheSeconds) {
        var cacheKey = getFullKey(key)
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

        return  stringCommand.opsForValue().decrement(cacheKey, value.AsLong())
    }
}



package nbcp.db.redis.proxy

import nbcp.base.extend.*
import nbcp.base.utils.SpringUtil
import nbcp.db.redis.BaseRedisProxy
import nbcp.db.redis.RedisRenewalTypeEnum
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.time.Duration

/**
 * Created by udi on 17-7-14.
 */
class RedisStringProxy(
        group: String,
        dbOffset: Int = 0,
        defaultCacheSeconds: Int = 0,
        renewalType: RedisRenewalTypeEnum = RedisRenewalTypeEnum.Write) :
        BaseRedisProxy( group, defaultCacheSeconds, renewalType) {


    fun get(key: String = ""): String {
        var cacheKey = getFullKey(key)
        var value = stringCommand.opsForValue().get(cacheKey)
        if (value == null) return "";
        readRenewalEvent(key)
        return value
    }

    fun set(key: String, value: String, cacheSecond: Int = defaultCacheSeconds)  {
        var cacheKey = getFullKey(key)

        if (cacheSecond <= 0) {
              stringCommand.opsForValue().set(cacheKey, value)
        } else {
             stringCommand.opsForValue().set(cacheKey, value, Duration.ofSeconds(cacheSecond.AsLong()))
        }
    }
}


class RedisNumberProxy(
        group: String,
        dbOffset: Int = 0,
        defaultCacheSeconds: Int = 0,
        renewalType: RedisRenewalTypeEnum = RedisRenewalTypeEnum.Write) :
        BaseRedisProxy( group, defaultCacheSeconds, renewalType) {

    fun get(key: String = ""): Long {
        var cacheKey = getFullKey(key)
        var value = stringCommand.opsForValue().get(cacheKey).AsLong()
        readRenewalEvent(key)
        return value.AsLong()
    }

    fun set(key: String, value: Long, cacheSecond: Int = defaultCacheSeconds)  {
        var cacheKey = getFullKey(key)
        if (cacheSecond <= 0) {
            stringCommand.opsForValue().set(cacheKey, value.toString())
        } else {
            stringCommand.opsForValue().set (cacheKey, value.toString(), Duration.ofSeconds( cacheSecond.AsLong()))
        }

    }


    fun incr(key: String = ""): Long {
        var cacheKey = getFullKey(key)
        if (cacheKey.isEmpty()) return -1L

        var ret = stringCommand.opsForValue().increment(cacheKey)
        writeRenewalEvent(key)
        return ret;
    }

    fun decr(key: String = ""): Long {
        var cacheKey = getFullKey(key)
        if (cacheKey.isEmpty()) return -1L

        var ret = stringCommand.opsForValue().decrement(cacheKey)
        writeRenewalEvent(key)
        return ret;
    }
}



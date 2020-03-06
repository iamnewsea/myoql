package nbcp.db.redis.proxy

import nbcp.base.extend.*
import nbcp.db.redis.BaseRedisProxy
import nbcp.db.redis.RedisRenewalTypeEnum

/**
 * Created by udi on 17-7-14.
 */
class RedisStringProxy(
        group: String,
        dbOffset: Int = 0,
        defaultCacheSeconds: Int = 0,
        renewalType: RedisRenewalTypeEnum = RedisRenewalTypeEnum.Write) :
        BaseRedisProxy(dbOffset, group, defaultCacheSeconds, renewalType) {

    fun get(key: String = ""): String {
        var cacheKey = getFullKey(key)
        var value = redis.stringCommand(dbOffset) { it.get(cacheKey) }
        if (value == null) return "";
        readRenewalEvent(key)
        return value
    }

    fun set(key: String, value: String, cacheSecond: Int = defaultCacheSeconds): String {
        var cacheKey = getFullKey(key)

        if (cacheSecond <= 0) {
            return redis.stringCommand(dbOffset) { it.set(cacheKey, value) }
        } else {
            return redis.stringCommand(dbOffset) { it.setex(cacheKey, cacheSecond.AsLong(), value) }
        }
    }
}


class RedisNumberProxy(
        group: String,
        dbOffset: Int = 0,
        defaultCacheSeconds: Int = 0,
        renewalType: RedisRenewalTypeEnum = RedisRenewalTypeEnum.Write) :
        BaseRedisProxy(dbOffset, group, defaultCacheSeconds, renewalType) {

    fun get(key: String = ""): Long {
        var cacheKey = getFullKey(key)
        var value = redis.stringCommand(dbOffset) { it.get(cacheKey) }
        readRenewalEvent(key)
        return value.AsLong()
    }

    fun set(key: String, value: Long, cacheSecond: Int = defaultCacheSeconds): Long {
        var cacheKey = getFullKey(key)
        if (cacheSecond <= 0) {
             redis.stringCommand(dbOffset) { it.set(cacheKey, value.toString()) }
        } else {
             redis.stringCommand(dbOffset) { it.setex(cacheKey, cacheSecond.AsLong(), value.toString()) }
        }
        return value;
    }


    fun incr(key: String = ""): Long {
        var cacheKey = getFullKey(key)
        if (cacheKey.isEmpty()) return -1L

        var ret = redis.stringCommand(dbOffset) { it.incr(cacheKey) }
        writeRenewalEvent(key)
        return ret;
    }

    fun decr(key: String = ""): Long {
        var cacheKey = getFullKey(key)
        if (cacheKey.isEmpty()) return -1L

        var ret = redis.stringCommand(dbOffset) { it.decr(cacheKey) }
        writeRenewalEvent(key)
        return ret;
    }
}



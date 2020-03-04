package nbcp.db.redis.proxy

import nbcp.base.extend.*
import nbcp.db.redis.BaseRedisProxy

/**
 * Created by udi on 17-7-14.
 */
class RedisStringProxy(
        group: String,
        dbOffset: Int = 0,
        var defaultCacheSeconds: Int = 0) : BaseRedisProxy(dbOffset, group) {

    fun get(key: String = ""): String {
        var cacheKey = getKey(key)
        var value = redis.stringCommand(dbOffset) { it.get(cacheKey) }
        if (value == null) return "";
        return value
    }

    fun set(key: String, value: String, cacheKeySeconds: Int = 0): String {
        var cs = cacheKeySeconds.AsInt(defaultCacheSeconds);

        var cacheKey = getKey(key)

        if (cs > 0) {
            return redis.stringCommand(dbOffset) { it.setex(cacheKey, cs.toLong(), value) }
        } else {
            return redis.stringCommand(dbOffset) { it.set(cacheKey, value); }
        }
    }

    fun set(value: String, cacheKeySeconds: Int = 0) = set("", value, cacheKeySeconds)

}


class RedisNumberProxy(
        group: String,
        dbOffset: Int = 0,
        var defaultCacheSeconds: Int = 0) : BaseRedisProxy(dbOffset, group) {

    fun get(key: String = ""): Long {
        var cacheKey = getKey(key)
        var value = redis.stringCommand(dbOffset) { it.get(cacheKey) }

        return value.AsLong()
    }

    fun set(key: String, value: Long, cacheKeySeconds: Int = 0): String {
        var cs = cacheKeySeconds.AsInt(defaultCacheSeconds);

        var cacheKey = getKey(key)

        if (cs > 0) {
            return redis.stringCommand(dbOffset) { it.setex(cacheKey, cs.toLong(), value.toString()) }
        } else {
            return redis.stringCommand(dbOffset) { it.set(cacheKey, value.toString()); }
        }
    }

    fun set(value: Long, cacheKeySeconds: Int = 0) = set("", value, cacheKeySeconds)


    fun incr(key: String = ""): Long {
        var cacheKey = getKey(key)
        if (cacheKey.isEmpty()) return -1L

        return redis.stringCommand(dbOffset) { it.incr(cacheKey) }
    }

    fun decr(key: String = ""): Long {
        var cacheKey = getKey(key)
        if (cacheKey.isEmpty()) return -1L

        return redis.stringCommand(dbOffset) { it.decr(cacheKey) }
    }
}



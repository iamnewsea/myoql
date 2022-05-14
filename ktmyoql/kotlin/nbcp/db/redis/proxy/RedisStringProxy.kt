package nbcp.db.redis.proxy

import nbcp.comm.*
import nbcp.utils.*
import nbcp.db.redis.BaseRedisProxy
import java.time.Duration

/**
 * Created by udi on 17-7-14.
 */
class RedisStringProxy @JvmOverloads constructor(
    group: String,
    defaultCacheSeconds: Int = 0,
    val autoRenewal: Boolean = false
) :
    BaseRedisProxy(group, defaultCacheSeconds) {


    fun get(key: String = ""): String {
        var cacheKey = getFullKey(key)
        var value = stringCommand.opsForValue().get(cacheKey)
        if (value == null) return "";

        if (autoRenewal) {
            renewalKey(key)
        }
        return value
    }

//    fun setKey(value: String, cacheSecond: Int = defaultCacheSeconds) = setKey("", value, cacheSecond);

    /**
     * @param cacheSecond: 0=默认值 , -1为不设置缓存时间
     */
    fun set(key: String, value: String, cacheSecond: Int = defaultCacheSeconds) {
        var cacheKey = getFullKey(key)

        if (cacheSecond < 0) {
            stringCommand.opsForValue().set(cacheKey, value)
        } else {
            stringCommand.opsForValue().set(cacheKey, value, Duration.ofSeconds(cacheSecond.AsLong()))
        }
    }


    /**
     * @param cacheSecond: 0=默认值 , -1为不设置缓存时间
     */
    fun setIfAbsent(key: String, value: String, cacheSecond: Int = defaultCacheSeconds): Boolean {
        var cacheKey = getFullKey(key)

        if (cacheSecond < 0) {
            return stringCommand.opsForValue().setIfAbsent(cacheKey, value)
        } else {
            return stringCommand.opsForValue()
                .setIfAbsent(cacheKey, value, Duration.ofSeconds(cacheSecond.AsLong(defaultCacheSeconds.AsLong())))
        }
    }
}

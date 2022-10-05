package nbcp.db.redis.proxy

import nbcp.comm.*
import nbcp.utils.*
import nbcp.db.redis.BaseRedisProxy
import java.time.Duration

/**
 * Created by udi on 17-7-14.
 */
class RedisStringProxy @JvmOverloads constructor(
    key: String,
    defaultCacheSeconds: Int = 0,
    autoRenewal: Boolean = false,
    // 自动续期,不能无限续期, 要设置创建时间,及绝对过期时间.
    // 在 创建时间  + 绝对过期时间秒数 < 当前时间 , 就强制过期了.
    //    var renewalExpiredSeconds: Int = 0
) :
    BaseRedisProxy(key, defaultCacheSeconds, autoRenewal) {


    fun get(): String {
        var cacheKey = getFullKey(key)
        var value = stringCommand.opsForValue().get(cacheKey)
        if (value == null) return "";

        if (autoRenewal) {
            renewalKey()
        }
        return value
    }

//    fun setKey(value: String, cacheSecond: Int = defaultCacheSeconds) = setKey("", value, cacheSecond);

    /**
     * @param cacheSecond: 0=默认值 , -1为不设置缓存时间
     */
    @JvmOverloads
    fun set(value: String, cacheSecond: Int = defaultCacheSeconds) {
        var cacheKey = getFullKey(key)

        this.defaultCacheSeconds = cacheSecond;

        if (cacheSecond < 0) {
            stringCommand.opsForValue().set(cacheKey, value)
        } else {
            stringCommand.opsForValue().set(cacheKey, value, Duration.ofSeconds(cacheSecond.AsLong()))
        }
    }


    /**
     * @param cacheSecond: 0=默认值 , -1为不设置缓存时间
     */
    @JvmOverloads
    fun setIfAbsent(value: String, cacheSecond: Int = defaultCacheSeconds): Boolean {
        var cacheKey = getFullKey(key)

        this.defaultCacheSeconds = cacheSecond;

        if (cacheSecond < 0) {
            return stringCommand.opsForValue().setIfAbsent(cacheKey, value)
        } else {
            return stringCommand.opsForValue()
                .setIfAbsent(cacheKey, value, Duration.ofSeconds(cacheSecond.AsLong(defaultCacheSeconds.AsLong())))
        }
    }
}

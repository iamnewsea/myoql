package nbcp.db.redis.proxy

import nbcp.base.extend.*
import nbcp.db.redis.BaseRedisProxy
import java.io.Serializable

/**
 * Created by yuxh on 2018/6/7
 */

class RedisBlobProxy(
        group: String,
        dbOffset: Int = 0,
        var defaultCacheSeconds: Int = 0): BaseRedisProxy(dbOffset,group) {


    fun get(key: String): Serializable? {
        var cacheKey = getKey(key)
        var ret = redis.byteArrayCommand(dbOffset){ it.get(cacheKey) }
        if (ret == null) return null
        return ret.ToSerializableObject()
    }


    fun set(key: String, value: Serializable, cacheSeconds: Int = 0) {
        var cacheKey = getKey(key)

        if (cacheSeconds <= 0) {
            redis.byteArrayCommand(dbOffset){ it.set(cacheKey, value.ToSerializableByteArray())}
        } else {
            redis.byteArrayCommand(dbOffset){ it.setex(cacheKey, cacheSeconds.toLong(), value.ToSerializableByteArray())}
        }
    }
}


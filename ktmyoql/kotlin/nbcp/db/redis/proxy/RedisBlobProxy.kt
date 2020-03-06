package nbcp.db.redis.proxy

import nbcp.base.extend.*
import nbcp.db.redis.BaseRedisProxy
import nbcp.db.redis.RedisRenewalTypeEnum
import java.io.Serializable

/**
 * Created by yuxh on 2018/6/7
 */

class RedisBlobProxy(
        group: String,
        dbOffset: Int = 0,
        defaultCacheSeconds: Int = 0,
        renewalType: RedisRenewalTypeEnum = RedisRenewalTypeEnum.Write)
    : BaseRedisProxy(dbOffset,group,defaultCacheSeconds,renewalType) {


    fun get(key: String): Serializable? {
        var cacheKey = getFullKey(key)
        var ret = redis.byteArrayCommand(dbOffset){ it.get(cacheKey) }
        if (ret == null) return null

        readRenewalEvent(key)
        return ret.ToSerializableObject()
    }


    fun set(key: String, value: Serializable) {
        var cacheKey = getFullKey(key)
        redis.byteArrayCommand(dbOffset){ it.set(cacheKey, value.ToSerializableByteArray())}

        writeRenewalEvent(key);
    }
}


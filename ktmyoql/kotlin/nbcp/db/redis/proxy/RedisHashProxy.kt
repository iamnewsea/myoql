package nbcp.db.redis.proxy

import nbcp.base.extend.ToMap
import nbcp.base.extend.*
import nbcp.db.redis.BaseRedisProxy
import nbcp.db.redis.RedisRenewalTypeEnum
import java.io.Serializable

/**
 * Created by yuxh on 2018/6/7
 */


class RedisHashProxy(
        group: String,
        dbOffset: Int = 0,
        defaultCacheSeconds: Int = 0,
        renewalType: RedisRenewalTypeEnum = RedisRenewalTypeEnum.Write)
    : BaseRedisProxy(dbOffset, group, defaultCacheSeconds, renewalType) {


    fun hkeys(key: String): List<String> {
        return redis.byteArrayCommand(dbOffset) {
            return@byteArrayCommand it.hkeys(key)
        }
    }

    fun getMap(key: String): Map<String, Serializable>? {
        return redis.byteArrayCommand(dbOffset) {
            var cacheKey = getFullKey(key)
            readRenewalEvent(key)
            return@byteArrayCommand it.hgetall(cacheKey).mapValues { it.value }
        }
    }

//    fun setMap(key: String, map: Map<String, String>) {
//        return redis.byteArrayCommand(dbOffset) { cmd ->
//            var cacheKey = getKey(key)
//            map.keys.forEach { k ->
//                cmd.hset(cacheKey, k, map.get(k)!!.toByteArray());
//            }
//            writeRenewalEvent(key)
//        }
//    }

    inline fun <reified T> getJson(key: String): T? {
        return getMap(key)?.ConvertJson(T::class.java)
    }


    fun get(key: String, field: String): ByteArray = redis.byteArrayCommand(dbOffset) {
        var cacheKey = getFullKey(key)
        readRenewalEvent(key)
        return@byteArrayCommand it.hget(cacheKey, field);
    }

//    private fun get(token_value: String): Map<String, Serializable> {
//        var cacheKey = getKey(token_value)
//
//        var keys = redis.byteArrayCommand(dbOffset) { it.hkeys(cacheKey) }
//        if (keys.any() == false) return linkedMapOf();
//        var ret = redis.byteArrayCommand(dbOffset) { it.hmget(cacheKey, *keys.toTypedArray()) }
//        readRenewalEvent(key)
//        return ret.ToMap({ it.key }, { it.value.ToSerializableObject() })
//    }

    fun setMap(key: String, value: Map<String, Serializable>): String {
        if (value.any() == false) return "";
        var cacheKey = getFullKey(key)

        var ret = redis.byteArrayCommand(dbOffset) { it.hmset(cacheKey, value.ToMap({ it.key }, { it.value.ToSerializableByteArray() })) }
        writeRenewalEvent(key)
        return ret;
    }
}
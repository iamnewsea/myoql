package nbcp.db.redis.proxy

import nbcp.comm.*
import nbcp.db.redis.BaseRedisProxy
import nbcp.db.redis.RedisRenewalTypeEnum
import java.io.Serializable

/**
 * Created by yuxh on 2018/6/7
 */


class RedisHashProxy(
        group: String,
        defaultCacheSeconds: Int = 0)
    : BaseRedisProxy(group, defaultCacheSeconds) {


    fun keys(key: String): Set<String> {
        var cacheKey = getFullKey(key)
        return anyTypeCommand.opsForHash<String, Any>().keys(cacheKey)
    }

    fun getMap(key: String): Map<String, Any> {
        var cacheKey = getFullKey(key)
        return anyTypeCommand.opsForHash<String, Any>().entries(cacheKey)
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


    fun getItem(key: String, field: String): Any? {
        var cacheKey = getFullKey(key)

        return anyTypeCommand.opsForHash<String, Any>().get(cacheKey, field)
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

    /**
     * 设置对象。 如果map为空，则删除。
     */
    fun setMap(key: String, value: Map<String, Any>) {
        if (value.any() == false) {
            super.deleteKeys(key);
            return;
        }
        var cacheKey = getFullKey(key)

        anyTypeCommand.opsForHash<String, Any>().putAll(cacheKey, value)
    }

    fun removeItems(key: String, vararg members: String) {
        var cacheKey = getFullKey(key)

        anyTypeCommand.opsForHash<String, Any>().delete(cacheKey, *members)
    }
}
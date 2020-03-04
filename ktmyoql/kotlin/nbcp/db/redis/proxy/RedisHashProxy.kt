package nbcp.db.redis.proxy

import nbcp.base.extend.ToMap
import nbcp.base.extend.*
import nbcp.comm.JsonMap
import nbcp.comm.StringMap
import nbcp.db.redis.BaseRedisProxy
import java.io.Serializable

/**
 * Created by yuxh on 2018/6/7
 */


class RedisHashProxy(
        group: String,
        dbOffset: Int = 0,
        var defaultCacheSeconds: Int = 0) : BaseRedisProxy(dbOffset, group) {


    fun hkeys(key: String): List<String> {
        return redis.byteArrayCommand(dbOffset) {
            return@byteArrayCommand it.hkeys(key)
        }
    }

    fun getMap(key: String): Map<String, String>? {
        return redis.byteArrayCommand(dbOffset) {
            it.hgetall(getKey(key)).mapValues { it.value.contentToString() }
        }
    }

    fun setMap(key: String, map: Map<String, String>) {
        return redis.byteArrayCommand(dbOffset) { cmd ->
            map.keys.forEach { k ->
                cmd.hset(getKey(key), k, map.get(k)!!.toByteArray());
            }
        }
    }

    inline fun <reified T> getJson(key: String): T? {
        return getMap(key)?.ConvertJson(T::class.java)
    }


    fun get(token_value: String, field: String): ByteArray = redis.byteArrayCommand(dbOffset) {
        it.hget(getKey(token_value), field);
    }

    private fun get(token_value: String): Map<String, Serializable> {
        var keys = redis.byteArrayCommand(dbOffset) { it.hkeys(token_value) }
        if (keys.any() == false) return linkedMapOf();
        var ret = redis.byteArrayCommand(dbOffset) { it.hmget(group + ":" + token_value, *keys.toTypedArray()) }
        return ret.ToMap({ it.key }, { it.value.ToSerializableObject() })
    }

    fun set(token_value: String, value: Map<String, Serializable>): String {
        if (value.any() == false) return "";

        return redis.byteArrayCommand(dbOffset) { it.hmset(group + ":" + token_value, value.ToMap({ it.key }, { it.value.ToSerializableByteArray() })) }
    }
}
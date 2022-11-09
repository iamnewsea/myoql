package nbcp.myoql.db.redis.proxy

import nbcp.base.extend.ConvertJson
import nbcp.myoql.db.redis.BaseRedisProxy

/**
 * Created by yuxh on 2018/6/7
 */


class RedisHashProxy @JvmOverloads constructor(
    key: String,
    defaultCacheSeconds: Int = 0,
    autoRenewal:Boolean = false
) : BaseRedisProxy(key, defaultCacheSeconds,autoRenewal) {


    fun keys(): Set<String> {
        var cacheKey = getFullKey(key)
        return stringCommand.opsForHash<String, Any>().keys(cacheKey)
    }

    fun getMap(): Map<String, Any> {
        var cacheKey = getFullKey(key)
        if(autoRenewal){
            renewalKey()
        }
        return stringCommand.opsForHash<String, Any>().entries(cacheKey)
    }


    /**
     * 增加其中的某一项。
     */
    @JvmOverloads
    fun increment(field: String, value: Long = 1): Long {
        var cacheKey = getFullKey(key);
        if(autoRenewal){
            renewalKey()
        }
        return stringCommand.opsForHash<String, Any>().increment(cacheKey, field, value)
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

    inline fun <reified T> getJson(): T? {
        return getMap().ConvertJson(T::class.java)
    }


    fun getItem(field: String): Any? {
        var cacheKey = getFullKey(key)
        if(autoRenewal){
            renewalKey()
        }
        return stringCommand.opsForHash<String, Any>().get(cacheKey, field)
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
     * 设置多个 key 值，不会删除其它key 。
     *
     */
    fun putMap(value: Map<String, Any>) {
        if (value.any() == false) return

        var cacheKey = getFullKey(key)
        if(autoRenewal){
            renewalKey()
        }
        stringCommand.opsForHash<String, Any>().putAll(cacheKey, value)
    }

    /**
     * 覆盖性设置 Hash，会删除其它key
     */
    fun resetMap(value: Map<String, Any>) {
        var keys = keys();
        var deleteKeys = keys - value.keys;
        removeItems(key, *deleteKeys.toTypedArray());

        putMap(value);
    }

    /**
     * 设置一个字段的值。
     */
    fun setItem(field: String, value: Any) {
        var cacheKey = getFullKey(key)
        if(autoRenewal){
            renewalKey()
        }
        stringCommand.opsForHash<String, Any>().put(cacheKey, field, value)
    }

    fun removeItems(vararg members: String) {
        if (members.any() == false) return;

        var cacheKey = getFullKey(key)
        if(autoRenewal){
            renewalKey()
        }
        stringCommand.opsForHash<String, Any>().delete(cacheKey, *members)
    }
}
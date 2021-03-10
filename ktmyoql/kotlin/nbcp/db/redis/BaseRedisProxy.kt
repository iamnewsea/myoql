package nbcp.db.redis

import nbcp.comm.AsInt
import nbcp.utils.*
import nbcp.comm.*
import org.springframework.data.redis.connection.StringRedisConnection
import org.springframework.data.redis.core.ScanOptions
import org.springframework.data.redis.core.StringRedisTemplate

enum class RedisRenewalTypeEnum {
    None, //不续期
    Read, //读取续期
    Write //写入续期
}


/**
 * 命令参考
 * http://doc.redisfans.com/
 * http://redisdoc.com/index.html
 * @param group: 系统推荐所有的Redis键，都要分组，带前缀！
 */
abstract class BaseRedisProxy(var group: String, var defaultCacheSeconds: Int) {
    companion object {
        @JvmStatic
        fun getFullKey(group: String, key: String): String {
            if (key.startsWith(group + ":")) return key;
            return arrayOf(group, key).filter { it.isNotEmpty() }.joinToString(":");
        }
    }

    protected val stringCommand: StringRedisTemplate by lazy {
        return@lazy SpringUtil.getBean<StringRedisTemplate>()
    }

    protected val anyTypeCommand: AnyTypeRedisTemplate by lazy {
        return@lazy SpringUtil.getBean<AnyTypeRedisTemplate>()
    }

    /**
     * 参数是 key,动态生成新的fullKey
     */
    private var _dynamic_group: ((String) -> String)? = null

    /**
     * 动态组，根据key
     */
    fun dynamicGroup(callback: ((String) -> String)) {
        this._dynamic_group = callback
    }

//    protected fun readRenewalEvent(key: String) {
//        if (renewalType == RedisRenewalTypeEnum.Read ||
//                renewalType == RedisRenewalTypeEnum.Write) {
//            return RedisTask.setExpireKey(key, defaultCacheSeconds);
//        }
//    }
//
//    protected fun writeRenewalEvent(key: String) {
//        if (renewalType == RedisRenewalTypeEnum.Write) {
//            return RedisTask.setExpireKey(key, defaultCacheSeconds);
//        }
//    }


//    companion object {
//        private val rediss = linkedMapOf<String, RedisCommands<String, *>>()
//
//        fun getStringRedisTemplate(db: Int): StringRedisTemplate  {
//            var redis = rediss.get(db.toString() + ":String") as StringRedisTemplate
//            if (redis == null) {
//                redis = SpringUtil.getBean<StringRedisTemplate>()
//
//                rediss.set(db.toString() + ":String", redis)
//            }
//            redis
//            return redis;
//        }
//    }

    fun getFullKey(key: String): String {
        var group2 = "";
        if( _dynamic_group != null){
            group2 = _dynamic_group!!.invoke(key)
        }
        else{
            group2 = group
        }

        if (key.startsWith(group2 + ":")) return key;
        return arrayOf(group2, key).filter { it.isNotEmpty() }.joinToString(":");
    }


    /**
     * 使用 scan 替代 keys
     */
    fun scan(pattern: String, limit: Int = 999): Set<String> {
        var list = mutableSetOf<String>()
        anyTypeCommand
            .connectionFactory
            .clusterConnection
            .use { conn ->
                conn.scan(
                    ScanOptions
                        .scanOptions()
                        .match(group + pattern)
                        .count(limit.AsLong())
                        .build()
                ).use { result ->
                    while (result.hasNext()) {
                        list.add(result.next().toString())
                    }
                }
            }
        return list;
    }

//    /**
//     * 对 group 键值续期
//     */
//    fun renewal(cacheSeconds: Int = defaultCacheSeconds) = renewalKey("", cacheSeconds);

    /**
     * 使用 RedisTask.setExpireKey 设置续期时间
     * @param key:不带group
     */
    fun renewalKey(key: String, cacheSeconds: Int = defaultCacheSeconds) {
        var cs = cacheSeconds.AsInt();
        if (cs <= 0) {
            RedisTask.deleteKeys(getFullKey(key))
            return;
        }

        RedisTask.setRenewalKey(getFullKey(key), cs);
    }

//    /**
//     * 删除 group 键值。
//     */
//    fun delete(): Long = deleteKeys("");


    /***
     * 删除键，使键过期。
     * 如果参数为空，则删除group键
     */
    fun deleteKeys(vararg keys: String): Long {
        var fullKeys = keys.map { getFullKey(it) }
        if (fullKeys.any() == false) {
            return 0;
        }
        RedisTask.deleteKeys(*fullKeys.toTypedArray());
        return anyTypeCommand.delete(fullKeys);
    }

    /**
     * 判断是否存在该Key
     */
    fun existsKey(key: String): Boolean = anyTypeCommand.hasKey(getFullKey(key));

//    /**
//     * 判断是否存在 group key
//     */
//    fun exists(): Boolean = existsKey("")
}
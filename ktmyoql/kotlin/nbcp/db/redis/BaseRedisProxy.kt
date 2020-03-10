package nbcp.db.redis

import io.lettuce.core.ScanArgs
import io.lettuce.core.ScanCursor
import nbcp.base.extend.AsInt
import nbcp.base.utils.SpringUtil
import nbcp.base.extend.*
import org.springframework.data.redis.connection.StringRedisConnection
import org.springframework.data.redis.core.StringRedisTemplate

enum class RedisRenewalTypeEnum {
    None, //不续期
    Read, //读取续期
    Write //写入续期
}


/**
 * @param autoRenewal: 续期，当有访问的时候
 */
abstract class BaseRedisProxy(val group: String, val defaultCacheSeconds: Int ) {
    companion object {
    }

    /**
     * 操作 key
     */
    val connection: StringRedisConnection by lazy {
        return@lazy anyTypeCommand.connectionFactory.connection as StringRedisConnection
    }


    protected val stringCommand: StringRedisTemplate by lazy {
        return@lazy SpringUtil.getBean<StringRedisTemplate>()
    }

    protected val anyTypeCommand: AnyTypeRedisTemplate by lazy {
        return@lazy SpringUtil.getBean<AnyTypeRedisTemplate>()
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
        if (key.startsWith(group + ":")) return key;
        return arrayOf(group, key).filter { it.isNotEmpty() }.joinToString(":");
    }


//    fun scan(pattern: String, limit: Int = 999): Set<String> {
//        var list = mutableSetOf<String>()
//        var batch = Math.min(limit, 20).AsLong();
//        var prevCusor = "0";
//
//
//            while (true) {
//                var result = keyCommand.scan(ScanCursor(prevCusor, false), ScanArgs.Builder.matches(group + pattern).limit(batch))
//
//                if (result.keys.any()) {
//                    list.addAll(result.keys)
//                }
//
//
//                if (result.cursor == "0") {
//                    break;
//                }
//
//                if (list.size >= limit) {
//                    break;
//                }
//
//                prevCusor = result.cursor
//            }
//
//        return list;
//    }

    /**
     * 服务器会禁用 keys
     */
//    fun keys(pattern: String): Set<String> {
//        return this.redis.stringCommand(dbOffset) { it.keys(pattern).toSet(); }
//    }


    /**
     * 使用 RedisTask.setExpireKey 设置过期时间
     * @param key:不带group
     */
    fun expireWithKey(key: String, cacheSeconds: Int = defaultCacheSeconds) {
        var cs = cacheSeconds.AsInt();
        if (cs <= 0) {
            return;
        }

        RedisTask.setExpireKey(getFullKey(key), cs);
    }

//    fun deleteWithKey(key: String): Long = connection.del(getFullKey(key));
//
//
//    /**
//     * 判断是否存在该Key
//     */
//    fun existsWithKey(key: String): Boolean = connection.exists(getFullKey(key));
}
package nbcp.db.redis

import io.lettuce.core.ScanArgs
import io.lettuce.core.ScanCursor
import nbcp.base.extend.AsInt
import nbcp.base.utils.SpringUtil
import nbcp.base.extend.*


abstract class BaseRedisProxy(protected val dbOffset: Int, var group: String) {
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

    fun getKey(key: String): String {
        return arrayOf(group, key).filter { it.isNotEmpty() }.joinToString(":");
    }

    //    abstract protected open val redis: RedisCommands<String, *>;
    protected val redis by lazy {
        return@lazy SpringUtil.getBean<RedisConfig>()
    }

    fun scan(pattern: String, limit: Int = 999): Set<String> {
        var list = mutableSetOf<String>()
        var batch = Math.min(limit, 20).AsLong();
        var prevCusor = "0";
        this.redis.stringCommand(dbOffset) {
            while (true) {
                var result = it.scan(ScanCursor(prevCusor, false), ScanArgs.Builder.matches(group + pattern).limit(batch))

                if (result.keys.any()) {
                    list.addAll(result.keys)
                }


                if (result.cursor == "0") {
                    break;
                }

                if (list.size >= limit) {
                    break;
                }

                prevCusor = result.cursor
            }
        }
        return list;
    }

    /**
     * 服务器会禁用 keys
     */
//    fun keys(pattern: String): Set<String> {
//        return this.redis.stringCommand(dbOffset) { it.keys(pattern).toSet(); }
//    }


    fun expireWithKey(key: String, cacheSeconds: Int): Boolean {
        var cs = cacheSeconds.AsInt(cacheSeconds);
        if (cs < 0) {
            cs = 0;
        }

        return this.redis.stringCommand(dbOffset) {
            it.expire(getKey(key), cs.toLong())
        }
    }

    fun deleteWithKey(key: String): Long = this.redis.stringCommand(dbOffset) {
        it.del(getKey(key));
    }


    /**
     * 判断是否存在该Key
     */
    fun existsWithKey(key: String): Boolean = this.redis.stringCommand(dbOffset) {
        it.exists(getKey(key)).toInt() == 1;
    }
}
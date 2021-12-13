package nbcp.db.redis

import nbcp.comm.AsInt
import nbcp.db.db
import org.springframework.data.redis.core.StringRedisTemplate
import java.util.concurrent.TimeUnit

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

    /**
     * 动态数据源：
     * 1. 配置文件
     * 3. 当前作用域
     * 4. 使用默认
     */
    protected val stringCommand: StringRedisTemplate
        get() {
            return db.redis.getStringRedisTemplate(group)
        }

    fun getFullKey(key: String): String {
        if (key.startsWith(group + ":")) return key;
        return arrayOf(group, key).filter { it.isNotEmpty() }.joinToString(":");
    }


    /**
     * 使用 RedisTask.setExpireKey 设置续期时间
     * @param key:不带group
     */
    @JvmOverloads
    fun renewalKey(key: String, cacheSeconds: Int = defaultCacheSeconds) {
        val cs = cacheSeconds.AsInt();
        if (cs <= 0) {
            RedisRenewalDynamicService.clearDelayRenewalKeys(getFullKey(key))
            return;
        }

        RedisRenewalDynamicService.setDelayRenewalKey(getFullKey(key), cs);
    }


    /***
     * 删除键，使键过期。
     * 如果参数为空，则删除group键
     */
    fun deleteKeys(vararg keys: String): Long {
        val fullKeys = keys.map { getFullKey(it) }
        if (fullKeys.any() == false) {
            return 0;
        }
        RedisRenewalDynamicService.clearDelayRenewalKeys(*fullKeys.toTypedArray());
        return stringCommand.delete(fullKeys);
    }

    /**
     * 判断是否存在该Key
     */
    fun existsKey(key: String): Boolean = stringCommand.hasKey(getFullKey(key));

    /**
     * 获取ttl，
     */
    fun getExpireSeconds(key: String): Int {
        return stringCommand.getExpire(key, TimeUnit.SECONDS).AsInt()
    }
}

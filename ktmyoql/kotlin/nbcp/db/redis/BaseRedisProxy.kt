package nbcp.db.redis

import nbcp.comm.AsInt
import nbcp.db.db
import org.springframework.data.redis.core.StringRedisTemplate
import java.util.concurrent.TimeUnit

/**
 * 命令参考
 * http://doc.redisfans.com/
 * http://redisdoc.com/index.html
 * @param group: 系统推荐所有的Redis键，都要分组，带前缀！
 */
abstract class BaseRedisProxy(var key: String, var defaultCacheSeconds: Int, var autoRenewal: Boolean) {

    init {
//        if (autoRenewal) {
//            renewalKey()
//        }
    }

    /**
     * 动态数据源：
     * 1. 配置文件
     * 3. 当前作用域
     * 4. 使用默认
     */
    protected val stringCommand: StringRedisTemplate
        get() {
            return db.redis.getStringRedisTemplate(key.split(":").first())
        }

    fun getFullKey(key: String): String {
//        if (key.startsWith(group + ":")) return key;
//        return arrayOf(group, key).filter { it.isNotEmpty() }.joinToString(":");
        return key;
    }


    /**
     * 使用 RedisTask.setExpireKey 设置续期时间
     * @param key:不带group
     */
    @JvmOverloads
    fun renewalKey(cacheSeconds: Int = defaultCacheSeconds) {
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
    fun deleteKey(): Boolean {
        RedisRenewalDynamicService.clearDelayRenewalKeys(key);
        return stringCommand.delete(key);
    }

    /**
     * 判断是否存在该Key
     */
    fun existsKey(): Boolean = stringCommand.hasKey(getFullKey(key));

    /**
     * 获取ttl，
     */
    fun getExpireSeconds(): Int {
        return stringCommand.getExpire(getFullKey(key), TimeUnit.SECONDS).AsInt()
    }
}

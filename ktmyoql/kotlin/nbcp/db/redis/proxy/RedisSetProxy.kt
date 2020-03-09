package nbcp.db.redis.proxy

import io.lettuce.core.ScanArgs
import nbcp.db.redis.BaseRedisProxy
import nbcp.db.redis.RedisRenewalTypeEnum

/**
 * Created by yuxh on 2018/6/7
 */

open class RedisSetProxy(
        group: String,
        dbOffset: Int = 0,
        defaultCacheSeconds: Int = 0,
        renewalType: RedisRenewalTypeEnum = RedisRenewalTypeEnum.Write) :
        BaseRedisProxy(  group, defaultCacheSeconds, renewalType) {


    /**
     * 添加
     */
    fun add(key: String, vararg value: String) {
        if (value.any() == false) return
        var cacheKey = getFullKey(key);
        anyTypeCommand.opsForSet().add(cacheKey, *value)

        writeRenewalEvent(key)
    }

    /**
     * 成员数量
     */
    fun scard(key: String): Int {
        var cacheKey = getFullKey(key);
        return anyTypeCommand.opsForSet().size(cacheKey).toInt()
    }

    fun isMember(key: String, member: String): Boolean {
        var cacheKey = getFullKey(key);
        return anyTypeCommand.opsForSet().isMember(cacheKey, member)
    }

    /**
     * 获取成员
     */
    fun getSet(key: String): Set<String> {
        var cacheKey = getFullKey(key);
        return redis.stringCommand(dbOffset) { it.smembers(cacheKey) }
    }

    fun spop(key: String): String {
        var cacheKey = getFullKey(key);
        writeRenewalEvent(key)
        return redis.stringCommand(dbOffset) { it.spop(cacheKey) ?: "" }
    }

    fun sscan(key: String, member: String, limit: Int): List<String> {
        var cacheKey = getFullKey(key);
        return redis.stringCommand(dbOffset) {
            it.sscan(cacheKey, ScanArgs.Builder.matches(member).limit(limit.toLong())).values
        }
    }

    /**
     * 删除成员
     * 返回删除的成员个数。
     */
    fun remove(key: String, vararg members: String): Long {
        if( members.any()== false) return 0;
        var cacheKey = getFullKey(key);
        var ret = redis.stringCommand(dbOffset) { it.srem(cacheKey, *members) }
        writeRenewalEvent(key)
        return ret;
    }
}
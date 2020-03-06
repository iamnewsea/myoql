package nbcp.db.redis.proxy

import io.lettuce.core.Limit
import io.lettuce.core.Range
import io.lettuce.core.ScanArgs
import nbcp.db.redis.BaseRedisProxy
import nbcp.db.redis.RedisRenewalTypeEnum

/**
 * Created by yuxh on 2018/6/7
 */

open class RedisSortedSetProxy(
        group: String,
        dbOffset: Int = 0,
        defaultCacheSeconds: Int = 0,
        renewalType: RedisRenewalTypeEnum = RedisRenewalTypeEnum.Write) :
        BaseRedisProxy(dbOffset, group, defaultCacheSeconds, renewalType) {

    fun add(key: String, member: String, score: Double) {
        var cacheKey = getFullKey(key);
        redis.stringCommand(dbOffset) { it.zadd(cacheKey, score, member) }
        writeRenewalEvent(key)
    }

    fun add(key: String, vararg value: Pair<String, Double>) {
        var cacheKey = getFullKey(key);
        var list = mutableListOf<Any>()
        value.forEach {
            list.add(it.second)
            list.add(it.first)
        }
        if (list.any() == false) return

        redis.stringCommand(dbOffset) { it.zadd(group, *list.toTypedArray()) }
        writeRenewalEvent(key)
    }

    /**
     *
     */
    fun getItem(key: String, minScore: Long, maxScore: Long): String {
        var cacheKey = getFullKey(key);
        readRenewalEvent(key)
        return redis.stringCommand(dbOffset) {
            it.zrangebyscore(cacheKey, Range.create(minScore, maxScore), Limit.create(0, 1)).firstOrNull() ?: ""
        }
    }

    /**
     * 按分值获取区间
     */
    fun getListByScore(key: String, minScore: Long, maxScore: Long): List<String> {
        var cacheKey = getFullKey(key);
        readRenewalEvent(key)
        return redis.stringCommand(dbOffset) { it.zrangebyscore(cacheKey, Range.create(minScore, maxScore)) }
    }

    /**
     * 按索引获取区间
     */
    fun getListByIndex(key: String, start: Int, end: Int): List<String> {
        var cacheKey = getFullKey(key);
        readRenewalEvent(key)
        return redis.stringCommand(dbOffset) { it.zrange(cacheKey, start.toLong(), end.toLong()) }
    }

    /**
     * 按索引取第一个
     */
    fun getItem(key: String): String {
        var cacheKey = getFullKey(key);
        readRenewalEvent(key)
        return redis.stringCommand(dbOffset) { it.zrange(cacheKey, 0, 0).firstOrNull() ?: "" }
    }

    /**
     * 获取分值
     */
    fun getScore(key: String, member: String): Double {
        var cacheKey = getFullKey(key);
        readRenewalEvent(key)
        return redis.stringCommand(dbOffset) { it.zscore(cacheKey, member) ?: 0.toDouble() }
    }

    fun zscan(key: String, member: String, limit: Int): List<String> {
        var cacheKey = getFullKey(key);
        return redis.stringCommand(dbOffset) {
            it.zscan(cacheKey, ScanArgs.Builder.matches(member).limit(limit.toLong())).values.map { it.value }
        }
    }

//    fun existMember(member: String) = zscore(member) != 0.toDouble()

    /**
     * 移除
     */
    fun remove(key: String, vararg members: String): Long {
        var cacheKey = getFullKey(key);
        var ret = redis.stringCommand(dbOffset) { it.zrem(cacheKey, *members) }
        writeRenewalEvent(key)
        return ret;
    }
}
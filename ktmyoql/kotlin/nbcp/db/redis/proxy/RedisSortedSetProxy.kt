package nbcp.db.redis.proxy

import nbcp.comm.AsInt
import nbcp.comm.AsString
import nbcp.db.redis.BaseRedisProxy
import nbcp.db.redis.RedisRenewalTypeEnum
import org.springframework.data.redis.core.DefaultTypedTuple
import org.springframework.data.redis.core.ScanOptions
import org.springframework.data.redis.core.ZSetOperations

/**
 * 有序集合，是按 分值排序。
 */
open class RedisSortedSetProxy @JvmOverloads constructor(
        group: String,
        defaultCacheSeconds: Int = 0) :
        BaseRedisProxy(group, defaultCacheSeconds) {

    fun add(key: String, member: String, score: Double) {
        val cacheKey = getFullKey(key);
        stringCommand.opsForZSet().add(cacheKey, member, score)
    }

    fun add(key: String, vararg value: Pair<String, Double>) {
        if (value.any() == false) return
        val cacheKey = getFullKey(key);

        val set = value.map { DefaultTypedTuple(it.first, it.second)  }.toSet()
        stringCommand.opsForZSet().add(cacheKey, set)
    }

    fun size(key: String, member: String): Int {
        val cacheKey = getFullKey(key);
        return stringCommand.opsForZSet().size(cacheKey).AsInt();
    }

    fun isMember(key: String, member: String): Boolean {
        val cacheKey = getFullKey(key);
        return stringCommand.opsForZSet().score(cacheKey, member) != null
    }

    /**
     *
     */
    fun getItem(key: String, minScore: Double, maxScore: Double): String {
        var cacheKey = getFullKey(key);
        return stringCommand.opsForZSet().rangeByScore(cacheKey, minScore, maxScore, 0L, 1L).firstOrNull().AsString()

    }

    /**
     * 按分值获取区间
     */
    fun getListByScore(key: String, minScore: Double, maxScore: Double): List<String> {
        val cacheKey = getFullKey(key);
        return stringCommand.opsForZSet().rangeByScore(cacheKey, minScore, maxScore).map { it.AsString() }
    }

    /**
     * 按索引获取区间
     */
    fun getListByIndex(key: String, start: Int, end: Int): List<String> {
        val cacheKey = getFullKey(key);
        return stringCommand.opsForZSet().range(cacheKey, start.toLong(), end.toLong()).map { it.AsString() }
    }

    /**
     * 按索引取第一个
     */
    fun getItem(key: String): String {
        val cacheKey = getFullKey(key);
        return stringCommand.opsForZSet().range(cacheKey, 0L, 0L).firstOrNull().AsString()
    }

    /**
     * 获取分值
     */
    fun getScore(key: String, member: String): Double {
        val cacheKey = getFullKey(key);
        return stringCommand.opsForZSet().score(cacheKey, member)
    }

//    fun zscan(key: String, member: String, limit: Int): List<String> {
//        var cacheKey = getFullKey(key);
//        return stringCommand.opsForZSet()
//                .scan(cacheKey, ScanOptions.scanOptions().match(member).count(limit).build())
//                .
//            it.zscan(cacheKey, ScanArgs.Builder.matches(member).limit(limit.toLong())).values.map { it.value }
//        }
//    }

//    fun existMember(member: String) = zscore(member) != 0.toDouble()

    /**
     * 移除
     */
    fun removeItems(key: String, vararg members: String): Long {
        val cacheKey = getFullKey(key);
        return  stringCommand.opsForZSet().remove(cacheKey, *members)
    }
}
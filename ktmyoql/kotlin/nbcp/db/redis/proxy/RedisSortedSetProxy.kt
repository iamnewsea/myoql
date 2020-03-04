package nbcp.db.redis.proxy

import io.lettuce.core.ScanArgs
import nbcp.db.redis.BaseRedisProxy

/**
 * Created by yuxh on 2018/6/7
 */

open class RedisSortedSetProxy(
        group: String,
        dbOffset: Int = 0) : BaseRedisProxy(dbOffset, group) {

    fun zadd(member: String, score: Int) {
        redis.stringCommand(dbOffset) { it.zadd(group, score.toDouble(), member) }
    }

    fun zadd(vararg value: Pair<String, Int>) {
        var list = mutableListOf<Any>()
        value.forEach {
            list.add(it.second)
            list.add(it.first)
        }
        if (list.any() == false) return

        redis.stringCommand(dbOffset) { it.zadd(group, *list.toTypedArray()) }
    }

    fun zrange1(minScore: Long, maxScore: Long): String {
        return redis.stringCommand(dbOffset) { it.zrange(group, minScore, maxScore).firstOrNull() ?: "" }
    }

    fun zrange(minScore: Long, maxScore: Long): List<String> {
        return redis.stringCommand(dbOffset) { it.zrange(group, minScore, maxScore) }
    }

    fun zrange1(): String {
        return redis.stringCommand(dbOffset) { it.zrange(group, 0, 0).firstOrNull() ?: "" }
    }

    fun zscore(member: String): Double {
        return redis.stringCommand(dbOffset) { it.zscore(group, member) ?: 0.toDouble() }
    }

    fun zscan(member: String, limit: Int): List<String> {
        return redis.stringCommand(dbOffset) {
            it.zscan(group, ScanArgs.Builder.matches(member).limit(limit.toLong())).values.map { it.value }
        }
    }

//    fun existMember(member: String) = zscore(member) != 0.toDouble()

    fun zrem(vararg members: String): Long {
        return redis.stringCommand(dbOffset) { it.zrem(group, *members) }
    }
}
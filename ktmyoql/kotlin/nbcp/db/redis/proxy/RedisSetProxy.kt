package nbcp.db.redis.proxy

import io.lettuce.core.ScanArgs
import nbcp.db.redis.BaseRedisProxy

/**
 * Created by yuxh on 2018/6/7
 */

open class RedisSetProxy(
        group: String,
        dbOffset: Int = 0) : BaseRedisProxy(dbOffset, group) {


    fun sadd(vararg value: String) {
        if (value.any() == false) return
        redis.stringCommand(dbOffset) { it.sadd(group, *value) }
    }

    fun scard(): Int {
        return redis.stringCommand(dbOffset) { it.scard(group).toInt() }
    }

    fun sismember(member: String): Boolean {
        return redis.stringCommand(dbOffset) { it.sismember(group, member) }
    }

    fun smembers(): Set<String> {
        return redis.stringCommand(dbOffset) { it.smembers(group) }
    }

    fun spop(): String {
        return redis.stringCommand(dbOffset) { it.spop(group) ?: "" }
    }

    fun sscan(member: String, limit: Int): List<String> {
        return redis.stringCommand(dbOffset) {
            it.sscan(group, ScanArgs.Builder.matches(member).limit(limit.toLong())).values
        }
    }

    fun srem(vararg members: String): Long {
        return redis.stringCommand(dbOffset) { it.srem(group, *members) }
    }
}
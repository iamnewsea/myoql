package nbcp.db.redis.proxy

import nbcp.base.extend.*
import nbcp.db.redis.BaseRedisProxy
import nbcp.db.redis.RedisRenewalTypeEnum
import java.io.Serializable

/**
 * 列表，主要做队列用。
 */
class RedisListProxy(
        group: String,
        defaultCacheSeconds: Int = 0)
    : BaseRedisProxy(group, defaultCacheSeconds) {

    /**
     * 成员数量
     */
    fun size(key: String): Int {
        var cacheKey = getFullKey(key);
        return anyTypeCommand.opsForList().size(cacheKey).toInt().AsInt()
    }

    fun getIndex(key: String, index: Int): String {
        var cacheKey = getFullKey(key);
        return anyTypeCommand.opsForList().index(cacheKey, index.AsLong()).toString()
    }

    /**
     * 删除成员
     * 返回删除的成员个数。
     */
    fun remove(key: String, member: String): Int {
        var cacheKey = getFullKey(key);
        return anyTypeCommand.opsForList().remove(cacheKey, 0, member).AsInt()
    }

    /**
     * RPush，在最尾部添加。
     */
    fun Push(key: String, vararg members: String): Int {
        var cacheKey = getFullKey(key);
        return anyTypeCommand.opsForList().rightPushAll(cacheKey, *members).AsInt()
    }

    /**
     * RPop，在最尾部移除一个。
     */
    fun Pop(key: String): String {
        var cacheKey = getFullKey(key);
        return anyTypeCommand.opsForList().rightPop(cacheKey).AsString()
    }

    fun PopPush(key: String, targetGroup: String, targetKey: String): String {
        var cacheKey = getFullKey(key);
        var targetKey = BaseRedisProxy.getFullKey(targetGroup, targetKey)
        return anyTypeCommand.opsForList().rightPopAndLeftPush(cacheKey, targetKey).AsString()
    }

    /**
     * 获取列表。
     * @param start, 起始位置
     * @param end ,包含该索引的元素。-1表示包含最后一个索引。
     */
    fun getListString(key: String, start: Int = 0, end: Int = -1): List<String> {
        var cacheKey = getFullKey(key)
        return anyTypeCommand.opsForList().range(cacheKey, start.AsLong(), end.AsLong()).map { it.AsString() }
    }
}


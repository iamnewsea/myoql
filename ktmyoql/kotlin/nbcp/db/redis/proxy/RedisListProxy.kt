package nbcp.db.redis.proxy

import nbcp.comm.*
import nbcp.db.redis.BaseRedisProxy
import nbcp.db.redis.RedisRenewalTypeEnum
import java.io.Serializable

/**
 * 列表，主要做队列用。
 */
class RedisListProxy @JvmOverloads constructor(
        group: String,
        defaultCacheSeconds: Int = 0)
    : BaseRedisProxy(group, defaultCacheSeconds) {

    /**
     * 成员数量
     */
    fun size(key: String): Int {
        var cacheKey = getFullKey(key);
        return stringCommand.opsForList().size(cacheKey).toInt().AsInt()
    }

    fun getIndex(key: String, index: Int): String {
        var cacheKey = getFullKey(key);
        return stringCommand.opsForList().index(cacheKey, index.AsLong()).toString()
    }

    /**
     * 删除成员
     * 返回删除的成员个数。
     */
    fun removeItems(key: String, member: String): Int {
        var cacheKey = getFullKey(key);
        return stringCommand.opsForList().remove(cacheKey, 0, member).AsInt()
    }

    /**
     * RPush，在最尾部添加。
     */
    fun push(key: String, vararg members: String): Int {
        var cacheKey = getFullKey(key);
        return stringCommand.opsForList().rightPushAll(cacheKey, *members).AsInt()
    }

    /**
     * RPop，在最尾部移除一个。
     */
    fun pop(key: String): String {
        var cacheKey = getFullKey(key);
        return stringCommand.opsForList().rightPop(cacheKey).AsString()
    }

    fun popPush(key: String, targetGroup: String, targetKey: String): String {
        var cacheKey = getFullKey(key);
        var targetKey = BaseRedisProxy.getFullKey(targetGroup, targetKey)
        return stringCommand.opsForList().rightPopAndLeftPush(cacheKey, targetKey).AsString()
    }

    /**
     * 获取列表。
     * @param start, 起始位置
     * @param end ,包含该索引的元素。-1表示包含最后一个索引。
     */
    fun getListString(key: String, start: Int = 0, end: Int = -1): List<String> {
        var cacheKey = getFullKey(key)
        return stringCommand.opsForList().range(cacheKey, start.AsLong(), end.AsLong()).map { it.AsString() }
    }
}


package nbcp.myoql.db.redis.proxy

import nbcp.base.extend.AsInt
import nbcp.base.extend.AsLong
import nbcp.base.extend.AsString
import nbcp.myoql.db.redis.BaseRedisProxy

/**
 * 列表，主要做队列用。
 */
class RedisListProxy @JvmOverloads constructor(
    key: String,
    defaultCacheSeconds: Int = 0,
    autoRenewal:Boolean = false
) : BaseRedisProxy(key, defaultCacheSeconds, autoRenewal) {

    /**
     * 成员数量
     */
    fun size(): Int {
        var cacheKey = getFullKey(key);
        return stringCommand.opsForList().size(cacheKey).toInt().AsInt()
    }

    fun getIndex(index: Int): String {
        var cacheKey = getFullKey(key);
        return stringCommand.opsForList().index(cacheKey, index.AsLong()).toString()
    }

    /**
     * 删除成员
     * 返回删除的成员个数。
     */
    fun removeItems(member: String): Int {
        var cacheKey = getFullKey(key);
        if(autoRenewal){
            renewalKey()
        }
        return stringCommand.opsForList().remove(cacheKey, 0, member).AsInt()
    }

    /**
     * RPush，在最尾部添加。
     */
    fun push(vararg members: String): Int {
        var cacheKey = getFullKey(key);

        if(autoRenewal){
            renewalKey()
        }
        return stringCommand.opsForList().rightPushAll(cacheKey, *members).AsInt()
    }

    /**
     * RPop，在最尾部移除一个。
     */
    fun pop(): String {
        var cacheKey = getFullKey(key);

        if(autoRenewal){
            renewalKey()
        }
        return stringCommand.opsForList().rightPop(cacheKey).AsString()
    }

    /**
     * Rpoplpush 命令用于移除列表的最后一个元素，并将该元素添加到另一个列表并返回。
     * 如:  RpopLpush 列表1  列表2  ---> 把列表1的最后一个 移动 到列表2的末尾.
     */
    fun popPush(targetKey: String): String {
        var cacheKey = getFullKey(key);

        if(autoRenewal){
            renewalKey()
        }
        return stringCommand.opsForList().rightPopAndLeftPush(cacheKey, targetKey).AsString()
    }

    /**
     * 获取列表。
     * @param start, 起始位置
     * @param end ,包含该索引的元素。-1表示包含最后一个索引。
     */

    @JvmOverloads
    fun getListString(start: Int = 0, end: Int = -1): List<String> {
        var cacheKey = getFullKey(key)

        if(autoRenewal){
            renewalKey()
        }
        return stringCommand.opsForList().range(cacheKey, start.AsLong(), end.AsLong()).map { it.AsString() }
    }
}


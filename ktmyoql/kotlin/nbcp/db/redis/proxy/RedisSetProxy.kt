package nbcp.db.redis.proxy

import nbcp.comm.AsInt
import nbcp.comm.AsLong
import nbcp.db.redis.BaseRedisProxy
import org.springframework.data.redis.core.ScanOptions

/**
 * Created by yuxh on 2018/6/7
 */

open class RedisSetProxy @JvmOverloads constructor(
    key: String,
    defaultCacheSeconds: Int = 0
) :
    BaseRedisProxy(key, defaultCacheSeconds) {


    /**
     * 成员数量
     */
    fun size(): Int {
        val cacheKey = getFullKey(key);
        return stringCommand.opsForSet().size(cacheKey).toInt().AsInt()
    }

    fun isMember(member: String): Boolean {
        val cacheKey = getFullKey(key);
        return stringCommand.opsForSet().isMember(cacheKey, member)
    }

    /**
     * 删除成员
     * 返回删除的成员个数。
     */
    fun removeItems(vararg members: String): Long {
        if (!members.any()) return 0;
        val cacheKey = getFullKey(key);
        return stringCommand.opsForSet().remove(cacheKey, *members);
    }


    /**
     * 添加
     */
    fun add(vararg value: String) {
        if (value.any() == false) return
        var cacheKey = getFullKey(key);
        stringCommand.opsForSet().add(cacheKey, *value)
    }

    /**
     * 获取成员
     */
    fun getListString(): List<String> {
        var cacheKey = getFullKey(key);
        return stringCommand.opsForSet().members(cacheKey).map { it.toString() }
    }

    /**
     * 移除并返回一个随机元素
     */
    fun spop(): String? {
        var cacheKey = getFullKey(key);
        return stringCommand.opsForSet().pop(cacheKey)?.toString()
    }

    /**
     * 扫描
     */
    fun sscan(pattern: String, limit: Int = 999): Set<String> {

        var cacheKey = getFullKey(key);

        stringCommand.opsForSet()
            .scan(cacheKey, ScanOptions.scanOptions().match(pattern).count(limit.AsLong()).build())
            .use { result ->
                var list = mutableSetOf<String>()
                while (result.hasNext()) {
                    list.add(result.next().toString())
                }
                return list;
            }
    }


}
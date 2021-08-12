package nbcp.db.redis.proxy

import nbcp.comm.AsInt
import nbcp.comm.AsLong
import nbcp.db.redis.BaseRedisProxy
import nbcp.db.redis.RedisRenewalTypeEnum
import org.springframework.data.redis.core.ScanOptions

/**
 * Created by yuxh on 2018/6/7
 */

open class RedisSetProxy @JvmOverloads constructor(
        group: String,
        defaultCacheSeconds: Int = 0) :
        BaseRedisProxy(group, defaultCacheSeconds) {


    /**
     * 成员数量
     */
    fun size(key: String): Int {
        var cacheKey = getFullKey(key);
        return stringCommand.opsForSet().size(cacheKey).toInt().AsInt()
    }

    fun isMember(key: String, member: String): Boolean {
        var cacheKey = getFullKey(key);
        return stringCommand.opsForSet().isMember(cacheKey, member)
    }

    /**
     * 删除成员
     * 返回删除的成员个数。
     */
    fun removeItems(key: String, vararg members: String): Long {
        if (members.any() == false) return 0;
        var cacheKey = getFullKey(key);
        var ret = stringCommand.opsForSet().remove(cacheKey, *members);
        return ret;
    }


    /**
     * 添加
     */
    fun add(key: String, vararg value: String) {
        if (value.any() == false) return
        var cacheKey = getFullKey(key);
        stringCommand.opsForSet().add(cacheKey, *value)
    }

    /**
     * 获取成员
     */
    fun getListString(key: String): List<String> {
        var cacheKey = getFullKey(key);
        return stringCommand.opsForSet().members(cacheKey).map { it.toString() }
    }

    /**
     * 移除并返回一个随机元素
     */
    fun spop(key: String): String? {
        var cacheKey = getFullKey(key);
        return stringCommand.opsForSet().pop(cacheKey)?.toString()
    }

    /**
     * 扫描
     */
    fun sscan(key: String, pattern: String, limit: Int = 999): Set<String> {

        var cacheKey = getFullKey(key);

        stringCommand.opsForSet()
                .scan(cacheKey, ScanOptions.scanOptions().match(group + pattern).count(limit.AsLong()).build())
                .use { result ->
                    var list = mutableSetOf<String>()
                    while (result.hasNext()) {
                        list.add(result.next().toString())
                    }
                    return list;
                }
    }


}
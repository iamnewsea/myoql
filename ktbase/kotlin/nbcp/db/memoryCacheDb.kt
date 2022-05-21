package nbcp.db

import nbcp.comm.JsonMap
import nbcp.comm.plusSeconds
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.function.Supplier

/**
 * 常驻内存型缓存
 */
object memoryCacheDb {
    private val logger = LoggerFactory.getLogger(this::class.java)

    var warnEachCapacity: Int = 100
    var errorMaxCapacity: Int = 1000

    private val map = JsonMap()

    class CacheItem(var callback: Supplier<Any>, var cacheSeconds: Int = 180) {
        var data: Any? = null
        var addAt: LocalDateTime = LocalDateTime.now()
    }

    private fun addItem(key: String, cacheSeconds: Int = 180, callback: Supplier<Any>): Any {
        if (map.size > 0) {
            if (map.size >= errorMaxCapacity) {
                throw java.lang.RuntimeException("缓存数据已达到最大条数 ${map.size} 条!")
            }

            if (map.size % warnEachCapacity == 0) {
                logger.warn("缓存数据达到 ${map.size} 条!")
            }
        }

        var item = CacheItem(callback, cacheSeconds);
        map.put(key, item)
        return item.data ?: throw java.lang.RuntimeException("异常:刚添加的数据为空!");
    }

    fun getMemoryCacheData(key: String, cacheSeconds: Int = 180, callback: Supplier<Any>): Any? {
        var value = map.get(key) as CacheItem?
        if (value == null) {
            return addItem(key, cacheSeconds, callback);
        }

        /**
         * 如果过期
         */
        if (value.addAt.plusSeconds(value.cacheSeconds) > LocalDateTime.now()) {
            return addItem(key, cacheSeconds, callback);
        }

        if (value.data == null) {
            throw java.lang.RuntimeException("异常:缓存的数据为空!");
        }

        return value.data;
    }
}
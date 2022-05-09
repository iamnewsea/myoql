package nbcp.db

import nbcp.comm.JsonMap
import nbcp.comm.plusSeconds
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.function.Supplier

/**
 * 常驻内存型缓存
 */
object MemoryCacheDb {
    private val logger = LoggerFactory.getLogger(this::class.java)

    var warnEachCapacity: Int = 100
    var errorMaxCapacity: Int = 1000

    private val map = JsonMap()

    class CacheItem(var callback: Supplier<Any>, var cacheSeconds: Int = 180) {
        var data: Any? = null
        var addAt: LocalDateTime = LocalDateTime.now()
    }

    fun addItem(key: String, cacheSeconds: Int = 180, callback: Supplier<Any>) {
        if (map.size > 0) {
            if (map.size >= errorMaxCapacity) {
                throw java.lang.RuntimeException("缓存数据已达到最大条数 ${map.size} 条!")
            }

            if (map.size % warnEachCapacity == 0) {
                logger.warn("缓存数据达到 ${map.size} 条!")
            }
        }

        map.put(key, CacheItem(callback, cacheSeconds))
    }

    fun getItem(key: String): Any? {
        var value = map.get(key) as CacheItem?
        if (value == null) return null;

        /**
         * 如果过期
         */
        if (value.addAt.plusSeconds(value.cacheSeconds) > LocalDateTime.now()) {
            value.data = null
        }

        if (value.data == null) {
            value.data = value.callback.get()
        }

        return value.data;
    }
}
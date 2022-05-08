package nbcp.db

import nbcp.comm.JsonMap
import nbcp.comm.plusSeconds
import java.time.LocalDateTime
import java.util.function.Supplier

/**
 * 常驻内存型缓存
 */
object MemoryCacheDb {
    private val map = JsonMap()

    class CacheItem(var callback: Supplier<Any>, var cacheSeconds: Int = 180) {
        var data: Any? = null
        var addAt: LocalDateTime = LocalDateTime.now()
    }

    fun addItem(key: String, cacheSeconds: Int = 180, callback: Supplier<Any>) {
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
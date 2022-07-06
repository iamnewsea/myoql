package nbcp.db

import nbcp.comm.JsonMap
import nbcp.comm.StringKeyMap
import nbcp.comm.plusSeconds
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.function.Supplier


class MemoryCacheItem(var callback: Supplier<out Any>, var cacheSeconds: Int = 180) {
    var data: Any? = null
    var addAt: LocalDateTime = LocalDateTime.now()

    fun reloadData() {
        this.addAt = LocalDateTime.now();
        this.data = this.callback.get();
    }
}


/**
 * 常驻内存型缓存
 */
object memoryCacheDb {
    private val logger = LoggerFactory.getLogger(this::class.java)

    var warnEachCapacity: Int = 100
    var errorMaxCapacity: Int = 1000

    private val map = StringKeyMap<MemoryCacheItem>()


    /**
     * 清除以 key 开头的 所有项
     */
    fun brokeStartsWithMemoryCache(key: String): Int {
        return map.keys
            .filter { it.startsWith(key) }
            .map {
                this.brokeMemoryCache(it)
            }
            .filter { it }
            .count()
    }

    fun brokeMemoryCache(key: ((String) -> Boolean)): Int {
        return map.keys
            .filter(key)
            .map {
                this.brokeMemoryCache(it)
            }
            .filter { it }
            .count()
    }

    fun brokeMemoryCache(key: String): Boolean {
        val value = map.get(key)
        if (value == null) return false;

        value.addAt = LocalDateTime.now()
        value.data = null;
        return true;
    }

    /**
     * 添加数据缓存提供者, 不执行
     */
    fun addMemoryCacheSupplier(key: String, cacheSeconds: Int = 180, callback: Supplier<out Any>) {
        if (map.size > 0) {
            if (map.size >= errorMaxCapacity) {
                throw java.lang.RuntimeException("缓存数据已达到最大条数 ${map.size} 条!")
            }

            if (map.size % warnEachCapacity == 0) {
                logger.warn("缓存数据达到 ${map.size} 条!")
            }
        }

        map.put(key, MemoryCacheItem(callback, cacheSeconds))
    }

    /**
     * 获取缓存数据
     */
    inline fun <reified T : Any> getFromMemoryCache(key: String): T? {
        return getDataFromMemoryCache(key) as T?
    }

    /**
     * 获取缓存数据
     */
    fun getDataFromMemoryCache(key: String): Any? {
        var value = map.get(key)
        if (value == null) {
            return null;
        }

        if (value.data == null) {
            value.reloadData();
            return value.data;
        }

        /**
         * 如果过期
         */
        if (value.addAt.plusSeconds(value.cacheSeconds) > LocalDateTime.now()) {
            value.reloadData();

            return value.data;
        }


        return value.data;
    }
}
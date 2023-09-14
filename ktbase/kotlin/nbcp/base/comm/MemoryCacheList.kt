package nbcp.base.comm

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import nbcp.base.extend.AsString
import org.springframework.util.AntPathMatcher
import java.time.Duration


var myMemoryCaches = MemoryCacheList.myMemoryCaches;

/**
 * 基于 guava cache  的封装
 */
class MemoryCacheList() : ArrayList<Cache<String, Any>>() {
    companion object {
        /**
         * 写入30秒后过期
         */
        var expireAfterWrite30Secondes = CacheBuilder.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(30))
                .build<String, Any>()

        /**
         * 写入1分钟后过期
         */
        var expireAfterWrite1Minute = CacheBuilder.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(1))
                .build<String, Any>()

        /**
         * 写入1小时后过期
         */
        var expireAfterWrite1Hour = CacheBuilder.newBuilder()
                .expireAfterWrite(Duration.ofHours(1))
                .build<String, Any>()

        /**
         * 写入4小时后过期
         */
        var expireAfterWrite4Hours = CacheBuilder.newBuilder()
                .expireAfterWrite(Duration.ofHours(4))
                .build<String, Any>()

        /**
         * 写入12小时后过期
         */
        var expireAfterWrite12Hours = CacheBuilder.newBuilder()
                .expireAfterWrite(Duration.ofHours(12))
                .build<String, Any>()



        var myMemoryCaches = MemoryCacheList(
                expireAfterWrite30Secondes,
                expireAfterWrite1Minute,
                expireAfterWrite1Hour,
                expireAfterWrite4Hours,
                expireAfterWrite12Hours
        )


        inline fun <T> findTypedValueByKey(key: String): T? {
            return myMemoryCaches.findTypedValueByKey(key)
        }

        fun findValueByKey(key: String): Any? {
            return myMemoryCaches.findValueByKey(key)
        }


        /**
         * 按正则破坏key
         */
        fun brokeWithMatch(key: String): Set<String> {
            return myMemoryCaches.brokeWithMatch(key);
        }
    }


    constructor(vararg items: Cache<String, Any>) : this() {
        this.addAll(items);
    }

    inline fun <T> findTypedValueByKey(key: String): T? {
        return findValueByKey(key) as T?;
    }

    fun findValueByKey(key: String): Any? {
        for (it in this) {
            var ret = it.getIfPresent(key);
            if (ret != null) {
                return ret;
            }
        }
        return null;
    }


    /**
     * 按正则破坏key
     */
    private fun Cache<String, Any>.brokeItemWithMatch(key: String): Set<String> {
        var map = this.asMap();
        if (map.isNullOrEmpty()) {
            return setOf();
        }

        var keys = map.keys;
        if (keys.isEmpty()) {
            return setOf();
        }
        if (key.isNullOrEmpty()) {
            this.invalidateAll();
            return keys;
        }

        var match = AntPathMatcher(".");
        var list = keys.filter { match.match(key, it) }.toSet();
        if (list.isNullOrEmpty()) {
            return setOf();
        }
        this.invalidateAll(list);
        return list;
    }


    fun brokeWithMatch(key: String): Set<String> {
        var list = mutableSetOf<String>();
        for (it in this) {
            list.addAll(it.brokeItemWithMatch(key))
        }
        return list;
    }

}
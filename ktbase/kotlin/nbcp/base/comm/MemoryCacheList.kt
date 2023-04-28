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


        /**
         * 访问30秒后过期
         */
        var expireAfterAccess30Secondes = CacheBuilder.newBuilder()
                .expireAfterAccess(Duration.ofSeconds(30))
                .build<String, Any>()

        /**
         * 访问1分钟后过期
         */
        var expireAfterAccess1Minute = CacheBuilder.newBuilder()
                .expireAfterAccess(Duration.ofMinutes(1))
                .build<String, Any>()

        /**
         * 访问1小时后过期
         */
        var expireAfterAccess1Hour = CacheBuilder.newBuilder()
                .expireAfterAccess(Duration.ofHours(1))
                .build<String, Any>()

        /**
         * 访问4小时后过期
         */
        var expireAfterAccess4Hours = CacheBuilder.newBuilder()
                .expireAfterAccess(Duration.ofHours(4))
                .build<String, Any>()

        /**
         * 访问12小时后过期
         */
        var expireAfterAccess12Hours = CacheBuilder.newBuilder()
                .expireAfterAccess(Duration.ofHours(12))
                .build<String, Any>()


        var myMemoryCaches = MemoryCacheList(
                expireAfterWrite30Secondes,
                expireAfterWrite1Minute,
                expireAfterWrite1Hour,
                expireAfterWrite4Hours,
                expireAfterWrite12Hours,
                expireAfterAccess30Secondes,
                expireAfterAccess1Minute,
                expireAfterAccess1Hour,
                expireAfterAccess4Hours,
                expireAfterAccess12Hours,
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


        fun brokeWithMatch(key: String): List<String> {
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
    private fun Cache<String, Any>.brokeWithMatch(key: String): List<String> {
        var match = AntPathMatcher(".");
        var list = this.asMap()?.keys?.toSet()?.filter { match.match(key.AsString("*"), it) };
        if (list.isNullOrEmpty()) {
            return listOf();
        }
        this.invalidateAll(list);
        return list;
    }


    fun brokeWithMatch(key: String): List<String> {
        var list = mutableListOf<String>();
        for (it in this) {
            list.addAll(it.brokeWithMatch(key))
        }
        return list;
    }

}
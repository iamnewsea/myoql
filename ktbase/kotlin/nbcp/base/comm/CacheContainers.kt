package nbcp.base.comm

import com.google.common.cache.Cache
import org.springframework.util.AntPathMatcher

class CacheContainers() : ArrayList<Cache<String, Any>>() {

    constructor(vararg items: Cache<String, Any>) : this() {
        this.addAll(items);
    }

    inline fun <T> findByKey(key: String): T? {
        return findObjectByKey(key) as T?;
    }

    fun findObjectByKey(key: String): Any? {
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
    private fun  Cache<String, Any>.brokeWithMatch(key: String): List<String> {
        var match = AntPathMatcher(".");
        var list = this.asMap().keys.toSet().filter { match.match(key, it) };
        this.invalidateAll(list);
        return list;
    }


    fun brokeWithMatch(key: String): List<String> {
        var list = mutableListOf<String>();
        for( it in  this){
            list.addAll(it.brokeWithMatch(key))
        }
        return list;
    }
}
package nbcp.db.redis

import nbcp.comm.*
import nbcp.db.db
import nbcp.db.redis.proxy.*
import nbcp.utils.CodeUtil
import nbcp.utils.SpringUtil
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate

/**
 * 缓存
 * 如果封装方法，会有数据库操作
 */
class RedisBaseGroup {
    val sqlCacheBroker = RedisSetProxy("sc-broker")
//    class SqlCacheGroup {
//        val cacheSqlData get() = RedisStringProxy("")
//        fun brokeKeys(table: String) = RedisSetProxy("broke-keys:${table}")
//        val borkeKeysChangedVersion get() = RedisNumberProxy("borke-keys-changed-version")
//        val brokingTable get() = RedisStringProxy("broking-table")
//    }



    //城市数据，缓存两个小时
    val cityCodeName get() = RedisStringProxy("city-code-name", 7200)

    val nacosInstance get() = RedisHashProxy("nacos-ins")

    /**
     * 获取城市
     */
    fun getCityNameByCode(code: Int): String {
        var name = cityCodeName.get(code.toString())
        if (name.HasValue) {
            return name;
        }


        //如果是 mongo, 如果是 mysql

        name = db.mor_base.sysCity.queryByCode(code)
            .select { it.name }
            .toEntity(String::class.java) ?: "";

        if (name.HasValue) {
            cityCodeName.set(code.toString(), name);
        }

        return name;
    }

//    val sqlCache = SqlCacheGroup()


}
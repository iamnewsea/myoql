package nbcp.db.redis

import nbcp.db.redis.proxy.RedisHashProxy
import nbcp.db.redis.proxy.RedisNumberProxy
import nbcp.db.redis.proxy.RedisSetProxy
import nbcp.db.redis.proxy.RedisStringProxy

class RedisBaseGroup {

    class Oauth2Group {
        val authorize = RedisHashProxy("oauth2-token")
    }

    class CacheGroup {
        val cacheSqlData = RedisStringProxy("")
        fun brokeKeys(table: String) = RedisSetProxy("broke-keys:${table}")
        val borkeKeysChangedVersion = RedisNumberProxy("borke-keys-changed-version")
        val brokingTable = RedisStringProxy("broking-table")
    }


    val oauth2 = Oauth2Group()
    val cache = CacheGroup()
}
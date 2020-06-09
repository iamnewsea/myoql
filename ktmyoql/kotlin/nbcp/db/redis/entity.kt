package nbcp.db.redis

import nbcp.comm.*
import nbcp.utils.*
import nbcp.db.redis.proxy.*
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.math.exp

class RedisBaseGroup {
    val validateCode get() = RedisStringProxy("validateCode", 180);



    class CacheGroup {
        val cacheSqlData get() = RedisStringProxy("")
        fun brokeKeys(table: String)   = RedisSetProxy("broke-keys:${table}")
        val borkeKeysChangedVersion get() = RedisNumberProxy("borke-keys-changed-version")
        val brokingTable get() = RedisStringProxy("broking-table")
    }

    val cache = CacheGroup()
}
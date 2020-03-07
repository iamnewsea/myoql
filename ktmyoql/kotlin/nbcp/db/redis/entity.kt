package nbcp.db.redis

import nbcp.base.extend.*
import nbcp.db.redis.proxy.*
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.math.exp

class RedisBaseGroup {

    class CacheGroup {
        val cacheSqlData = RedisStringProxy("")
        fun brokeKeys(table: String) = RedisSetProxy("broke-keys:${table}")
        val borkeKeysChangedVersion = RedisNumberProxy("borke-keys-changed-version")
        val brokingTable = RedisStringProxy("broking-table")
    }
    val cache = CacheGroup()
}
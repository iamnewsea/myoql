package nbcp.db.redis

import nbcp.comm.*
import nbcp.utils.*
import nbcp.db.redis.proxy.*
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.math.exp

object RedisBaseGroup {
    val validateCode = RedisStringProxy("validateCode", 180);



    class CacheGroup {
        val cacheSqlData = RedisStringProxy("")
        fun brokeKeys(table: String) = RedisSetProxy("broke-keys:${table}")
        val borkeKeysChangedVersion = RedisNumberProxy("borke-keys-changed-version")
        val brokingTable = RedisStringProxy("broking-table")
    }

    val cache = CacheGroup()

    val wx = WxGroup
}
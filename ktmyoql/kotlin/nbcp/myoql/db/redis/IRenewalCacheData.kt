package nbcp.myoql.db.redis

import java.time.LocalDateTime

/**
 * 还没想好
 */
interface IRenewalCacheData {
    var cacheAddAt: LocalDateTime;
}

class DefaultRenewalStringCacheData : IRenewalCacheData {
    override var cacheAddAt: LocalDateTime = LocalDateTime.now()
    var value: String = ""
}


package nbcp.db.cache

import java.lang.annotation.Inherited

/**
 * Sql Update/Insert/Delete Cache
 */
@Inherited
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class BrokeRedisCache(
    /**
     * 破坏表
     */
    val table: String,
    /**
     * 破坏表的隔离键，如: "cityCode"
     */
    val key: String = "",
    /**
     * 破坏表的隔离键值，如: "010"
     */
    val value: String = ""
)
package nbcp.db.cache

import nbcp.comm.*
import java.lang.annotation.Inherited

/**
 * Sql Select Cache
 */
@Inherited
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CacheForSelect(
    val cacheSeconds: Int,
    /**
     * 缓存表
     */
    val table: String,
    /**
     * 缓存关联表
     */
    val joinTables: Array<String>,
    /**
     * 缓存表的隔离键或主键, 如:"cityCode"
     */
    val key: String = "",
    /**
     * 缓存表的隔离值,如: "010"
     */
    val value: String = "",

//    val sql: String = ""
) {
}


/**
 * Sql Update/Insert/Delete Cache
 */
@Inherited
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CacheForBroke(
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

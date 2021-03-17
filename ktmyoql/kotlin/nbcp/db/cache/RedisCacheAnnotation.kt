package nbcp.db.cache

import nbcp.comm.Defines
import nbcp.comm.StringMap
import java.lang.annotation.Inherited

/**
 * Sql Select Cache
 */
@Inherited
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CacheForSelect(
    val tables: Array<String>,
    val md5: String = "",
    //隔离键
    val key: String = "",
    //隔离值
    val value: String = ""
)

/**
 * Sql Update Cache
 */
@Inherited
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CacheForUpdate(val table: String, val key: String = "", val value: String = "")

/**
 * Sql Insert Cache
 */
@Inherited
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CacheForInsert(val table: String, val key: String = "", val value: String = "")

/**
 * Sql Delete Cache
 */
@Inherited
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CacheForDelete(val table: String, val key: String = "", val value: String = "")
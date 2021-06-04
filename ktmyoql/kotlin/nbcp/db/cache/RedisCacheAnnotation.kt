package nbcp.db.cache

import nbcp.comm.StringMap
import nbcp.comm.line_break
import nbcp.utils.Md5Util
import java.lang.annotation.Inherited

/**
 * Sql Select Cache
 */
@Inherited
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CacheForSelect(
    val table: String,
    val joinTables: Array<String>,
    //隔离键
    val key: String = "",
    //隔离值
    val value: String = ""
) {
}

/**
 * Sql Update/Insert/Delete Cache
 */
@Inherited
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CacheForBroke(val table: String, val key: String = "", val value: String = "")

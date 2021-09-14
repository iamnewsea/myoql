package nbcp.db.cache

import nbcp.comm.*
import nbcp.utils.Md5Util
import nbcp.utils.SpringUtil
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext
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


//-----------------------

/**
 * CacheForSelect 的数据类
 */
data class CacheForSelectData(
    var cacheSeconds: Int = 0,
    /**
     * 缓存表
     */
    var table: String = "",
    /**
     * 缓存关联表
     */
    var joinTables: Array<String> = arrayOf(),
    /**
     * 缓存表的隔离键, 如:"cityCode"
     */
    var key: String = "",
    /**
     * 缓存表的隔离值,如: "010"
     */
    var value: String = "",
    var sql: String = ""
) {
    companion object {
        fun of(cacheForSelect: CacheForSelect, sql: String, variableMap: JsonMap): CacheForSelectData {
            var spelExecutor = CacheSpelExecutor(variableMap);
            var ret = CacheForSelectData();
            ret.cacheSeconds = cacheForSelect.cacheSeconds;
            ret.table = spelExecutor.getVariableValue(cacheForSelect.table);
            ret.joinTables = cacheForSelect.joinTables;
            ret.key = spelExecutor.getVariableValue(cacheForSelect.key);
            ret.value = spelExecutor.getVariableValue(cacheForSelect.value);
            ret.sql = spelExecutor.getVariableValue(sql);
            return ret
        }
    }
}


data class CacheForBrokeData(
    var table: String = "",
    /**
     * 破坏表的隔离键，如: "cityCode"
     */
    var key: String = "",
    /**
     * 破坏表的隔离键值，如: "010"
     */
    var value: String = ""
) {
    companion object {
        fun of(cacheForBroke: CacheForBroke, variableMap: JsonMap): CacheForBrokeData {
            var spelExecutor = CacheSpelExecutor(variableMap);
            var ret = CacheForBrokeData();
            ret.table = spelExecutor.getVariableValue(cacheForBroke.table);
            ret.key = spelExecutor.getVariableValue(cacheForBroke.key);
            ret.value = spelExecutor.getVariableValue(cacheForBroke.value);
            return ret;
        }
    }
}

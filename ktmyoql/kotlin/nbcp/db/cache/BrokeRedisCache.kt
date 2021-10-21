package nbcp.db.cache

import nbcp.comm.IsSimpleType
import java.lang.annotation.Inherited
import kotlin.reflect.KClass

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
    val table: String = "",
    /**
     * 破坏表的隔离键，如: "cityCode"
     */
    val groupKey: String = "",
    /**
     * 破坏表的隔离键值，如: "010"
     */
    val groupValue: String = "",

    /**
     * 如果 table 为空，则使用 table = tableClass.name
     */
    val tableClass: KClass<*> = Boolean::class
) {
}

fun BrokeRedisCache.getTableName():String{
    var tableName = this.table

    if( tableName.isEmpty() && !this.tableClass.java.IsSimpleType()){
        tableName = this.tableClass.java.simpleName;
    }
    return tableName;
}
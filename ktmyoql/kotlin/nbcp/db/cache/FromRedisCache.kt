package nbcp.db.cache

import nbcp.comm.*
import java.lang.annotation.Inherited
import kotlin.reflect.KClass

/**
 * Sql Select Cache
 */
@Inherited
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class FromRedisCache(
        /**
         * 如果 table 为空，则使用 table = tableClass.name
         */
        val tableClass: KClass<*> = Boolean::class,

        /**
         * 缓存关联表
         */
        val joinTableClasses: Array<KClass<*>> = arrayOf(),

        /**
         * 缓存表的隔离键或主键, 如:"cityCode"
         */
        val groupKey: String = "",
        /**
         * 缓存表的隔离值,如: "010"
         */
        val groupValue: String = "",

        /**
         * 唯一值
         */
        val sql: String = "",

        val cacheSeconds: Int = 3600,
        /**
         * 缓存表
         */
        val table: String = "",
        /**
         * 缓存关联表
         */
        val joinTables: Array<String> = arrayOf(),
) {
}


fun FromRedisCache.getTableName(): String {
    var tableName = this.table

    if (tableName.isEmpty() && !this.tableClass.java.IsSimpleType()) {
        tableName = this.tableClass.java.simpleName;
    }
    return tableName;
}

fun FromRedisCache.getJoinTableNames(): Array<String> {
    var joinTables = this.joinTables;

    if (joinTables.isEmpty() && !this.joinTableClasses.any()) {
        joinTables = this.joinTableClasses.map { it.simpleName!! }.toTypedArray()
    }
    return joinTables;
}



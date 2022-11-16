package nbcp.base.db.annotation

import java.lang.annotation.Inherited


/**
 * 数据表索引，注解可以继承
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@java.lang.annotation.Repeatable(DbEntityIndexes::class)
@Repeatable
@Inherited
annotation class DbEntityIndex(vararg val value: String, val unique: Boolean = false, val cacheable: Boolean = false)

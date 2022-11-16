package nbcp.base.db.annotation

import java.lang.annotation.Inherited

/**
 * 数据表索引，注解可以继承
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class DbEntityIndexes(vararg val value: DbEntityIndex)
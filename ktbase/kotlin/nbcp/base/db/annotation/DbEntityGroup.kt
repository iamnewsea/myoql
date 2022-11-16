package nbcp.base.db.annotation

import java.lang.annotation.Inherited

/**
 * 实体的组
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class DbEntityGroup(val value: String)
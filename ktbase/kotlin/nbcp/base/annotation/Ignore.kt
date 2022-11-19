package nbcp.base.annotation

import java.lang.annotation.Inherited

/**
 * 忽略字段
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class Ignore(val value: String = "")
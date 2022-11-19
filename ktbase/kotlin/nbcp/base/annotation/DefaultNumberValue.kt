package nbcp.base.annotation

import java.lang.annotation.Inherited

/**
 * 参数注解，默认的数值。
 */
@kotlin.annotation.Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE_PARAMETER, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class DefaultNumberValue(val value: Int = 0)
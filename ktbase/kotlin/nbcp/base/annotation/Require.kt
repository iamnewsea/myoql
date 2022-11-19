package nbcp.base.annotation

import java.lang.annotation.Inherited

/**
 * 必传字段，用于Mvc请求参数的注解，标记了该注解，表示该参数不能为空字符串，不能为空值。
 * ==  Validated   NotEmpty 两个注解的组合
 */
@kotlin.annotation.Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE_PARAMETER, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class Require(val value: String = "")
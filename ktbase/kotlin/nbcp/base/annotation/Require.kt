package nbcp.base.annotation

import nbcp.base.annotation.validator.RequireValidator
import java.lang.annotation.Documented
import java.lang.annotation.Inherited
import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass

/**
 * 必传字段，用于Mvc请求参数的注解，标记了该注解，表示该参数不能为空字符串，不能为空值。
 * ==  Validated    结合 @NotNull 和  @NotEmpty 两个注解的组合
 */
@kotlin.annotation.Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE_PARAMETER, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@Documented
@Constraint(validatedBy = arrayOf(RequireValidator::class))
annotation class Require(
        val message: String = "必传项!",
        val groups: Array<KClass<*>> = [],
        val payload: Array<KClass<out Payload>> = []
)
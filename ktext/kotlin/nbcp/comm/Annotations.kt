package nbcp.comm

import org.springframework.stereotype.Component
import java.lang.annotation.ElementType
import java.lang.annotation.Inherited
import java.security.KeyPair
import kotlin.reflect.KClass

/**
 * Created by udi on 17-3-30.
 */


/**
 * 必传字段，用于Mvc请求参数的注解，标记了该注解，表示该参数不能为空字符串，不能为空值。
 */
@kotlin.annotation.Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE_PARAMETER, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
//@Repeatable
annotation class Require(val value: String = "")

/**
 * 忽略字段
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Ignore(val value: String = "")


//@Repeatable
//@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE, AnnotationTarget.CLASS )
//@Retention(AnnotationRetention.RUNTIME)
//annotation class Setted(val settedFunc: String = "")
//
//
//@Repeatable
//@Target(AnnotationTarget.FIELD,AnnotationTarget.TYPE, AnnotationTarget.CLASS)
//@Retention(AnnotationRetention.RUNTIME)
//annotation class Setting(val settingFunc: String = "")

open class RequireException(var key:String) : Exception("${key} 为必填项")
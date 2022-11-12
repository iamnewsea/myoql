package nbcp.base.annotation

import nbcp.base.enums.LogLevelScopeEnum
import java.lang.annotation.Inherited


/**
 * 记录Action日志级别的注解，value = ch.qos.logback.classic.Level.级别
 * Level.toLevel识别的参数，不区分大小写，如：all|trace|debug|info|error|off
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class MyLogLevel(val value: LogLevelScopeEnum)

/**
 * Created by udi on 17-3-30.
 */

//@Target(AnnotationTarget.CLASS)
//@Retention(AnnotationRetention.RUNTIME)
//annotation class AutoLoadBean()

/**
 * 必传字段，用于Mvc请求参数的注解，标记了该注解，表示该参数不能为空字符串，不能为空值。
 */
@kotlin.annotation.Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE_PARAMETER, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class Require(val value: String = "")

/**
 * 参数注解，默认的数值。
 */
@kotlin.annotation.Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE_PARAMETER, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class DefaultNumberValue(val value: Int = 0)

/**
 * 忽略字段
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
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

open class RequireException(var key: String) : Exception("${key} 为必填项")


/**
 * 定时任务组件注解
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class GroupLog(val value: String = "")
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
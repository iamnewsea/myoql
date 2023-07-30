package nbcp.mvc.annotation

import java.lang.annotation.Inherited

/**
 * 停止记录日志
 * 标注了该标记的方法，第一次访问时，会正常记录，之后，会把它加入到忽略日志列表，后面就不会记录日志了。
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class StopLog
package nbcp.app

import nbcp.comm.using
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.core.annotation.AliasFor
import org.springframework.stereotype.Component

/**
 * 定时任务组件注解
 */
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Component
annotation class ScheduledTask(@get:AliasFor(annotation = Component::class) val value: String = "")

class ScheduledTaskScope() {}

@Aspect
@Component
class ScheduledTaskIntercepter {
    @Around("@within(nbcp.app.ScheduledTask)")
    fun intercept(joinPoint: ProceedingJoinPoint): Any? {
        return using(ScheduledTaskScope()) {
            var args = joinPoint.args
            if (args.any()) {
                return@using joinPoint.proceed(args)
            } else {
                return@using joinPoint.proceed()
            }
        }
    }
}

package nbcp.app

import nbcp.comm.usingScope
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.MDC
import org.springframework.core.annotation.AliasFor
import org.springframework.stereotype.Component

/**
 * 定时任务组件注解
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class GroupLog(val value: String = "")

@Aspect
@Component
class GroupLogIntercepter {
    @Around("@within(nbcp.app.GroupLog) || @annotation(nbcp.app.GroupLog)")
    fun intercept(joinPoint: ProceedingJoinPoint): Any? {
        var targetClass = joinPoint.target.javaClass;
        var signature = joinPoint.signature as MethodSignature;
        var method = targetClass.getDeclaredMethod(signature.name, *signature.parameterTypes)

        var groupLog = method.getAnnotation(GroupLog::class.java) ?: targetClass.getAnnotation(GroupLog::class.java);

        if (groupLog == null) {
            return invoke(joinPoint);
        }

        return usingScope(groupLog) {
            MDC.put("group", groupLog.value)
            return@usingScope invoke(joinPoint);
        }
    }

    private fun invoke(joinPoint: ProceedingJoinPoint): Any? {
        var args = joinPoint.args
        return joinPoint.proceed(args)
    }
}
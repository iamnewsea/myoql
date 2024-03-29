package nbcp.comm

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.MDC

/**
 * 定时任务组件注解
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class GroupLog(val value: String = "")



@Aspect
//@Component
class GroupLogAopService {
    @Around("@within(nbcp.comm.GroupLog) || @annotation(nbcp.comm.GroupLog)")
    fun intercept(joinPoint: ProceedingJoinPoint): Any? {
        var targetClass = joinPoint.target.javaClass;
        var signature = joinPoint.signature as MethodSignature;
        var method = targetClass.getDeclaredMethod(signature.name, *signature.parameterTypes)

        var groupLog = method.getAnnotation(GroupLog::class.java) ?: targetClass.getAnnotation(GroupLog::class.java);

        if (groupLog == null) {
            return joinPoint.proceed(joinPoint.args)
        }

        return usingScope(GroupLogScope.of(groupLog)) {
            MDC.put("group", groupLog.value)
            return@usingScope joinPoint.proceed(joinPoint.args)
        }
    }
}
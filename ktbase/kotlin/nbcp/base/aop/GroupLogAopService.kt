package nbcp.base.aop

import nbcp.base.annotation.GroupLog
import nbcp.base.scope.GroupLogScope
import nbcp.base.extend.usingScope
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.MDC
import java.lang.annotation.Inherited


@Aspect
//@Component
class GroupLogAopService {
    @Around("@within(nbcp.base.annotation.GroupLog) || @annotation(nbcp.base.annotation.GroupLog)")
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
            var ret = joinPoint.proceed(joinPoint.args)
            MDC.remove("group");
            return@usingScope ret;
        }
    }
}
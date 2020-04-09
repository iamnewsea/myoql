package nbcp.filter

import nbcp.base.extend.LogScope
import nbcp.base.extend.using
import nbcp.comm.MyLogLevel
import nbcp.web.HttpContext
import nbcp.web.findParameterValue
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.stereotype.Component
import reactor.core.publisher.SignalType

/**
 * 对注解了 MyLogLevel 的Bean或方法，使用 using(LogScope.NoLog) 包裹
 */
@Aspect
@Component
class LogLevelIntercepter {
    @Around("@within(nbcp.comm.MyLogLevel) || @annotation(nbcp.comm.MyLogLevel)")
    fun mongo(joinPoint: ProceedingJoinPoint): Any? {
        var signature = joinPoint.signature as MethodSignature;
        var targetType = signature.declaringType
        var method = targetType.getDeclaredMethod(signature.name, *signature.parameterTypes);
        var level = method.getAnnotationsByType(MyLogLevel::class.java).firstOrNull()
        if (level == null) {
            level = targetType.getAnnotationsByType<MyLogLevel>(MyLogLevel::class.java).firstOrNull() as MyLogLevel
        }

        if (level == null) {
            var args = joinPoint.args
            if (args.any()) {
                return joinPoint.proceed(args)
            } else {
                return joinPoint.proceed()
            }
        }

        return using(LogScope(level.logLevel)) {
            var args = joinPoint.args
            if (args.any()) {
                return joinPoint.proceed(args)
            } else {
                return joinPoint.proceed()
            }
        }
    }
}

package nbcp.base.aop

import nbcp.base.extend.usingScope
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature


/**
 * 对注解了 MyLogLevel 的Bean或方法，使用 usingScope(LogLevel.NoLog) 包裹
 */
@Aspect
//@Component
open class LogLevelAopService {

    //@annotation 表示拦截方法级别上的注解。
    //@within 表示拦截类级别上的注解。

    /**
     * 日志拦截
     */
    @Around("@within(nbcp.base.comm.MyLogLevel) || @annotation(nbcp.base.comm.MyLogLevel)")
    fun logPoint(joinPoint: ProceedingJoinPoint): Any? {
        val signature = joinPoint.signature as MethodSignature;
        val method = signature.method
        var level = method.getAnnotationsByType(nbcp.base.comm.MyLogLevel::class.java).firstOrNull()
        if (level == null) {
            val targetType = signature.declaringType
            level = targetType.getAnnotationsByType<nbcp.base.comm.MyLogLevel>(nbcp.base.comm.MyLogLevel::class.java)
                .firstOrNull() as nbcp.base.comm.MyLogLevel?
        }

        if (level == null) {
            return joinPoint.proceed(joinPoint.args)
        }

        usingScope(level.value) {
            return joinPoint.proceed(joinPoint.args)
        }
    }
}

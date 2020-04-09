package nbcp.filter

import nbcp.base.extend.LogScope
import nbcp.base.extend.using
import nbcp.web.HttpContext
import nbcp.web.findParameterValue
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component

/**
 * 对注解了 NoLog 的Bean或方法，使用 using(LogScope.NoLog) 包裹
 */
@Aspect
@Component
class LogOffIntercepter {
    @Around("@within(nbcp.comm.NoLog) || @annotation(nbcp.comm.NoLog)")
    fun mongo(joinPoint: ProceedingJoinPoint): Any? {
        return using(LogScope.LogOff) {
            var args = joinPoint.args
            if (args.any()) {
                return@using joinPoint.proceed(args)
            } else {
                return@using joinPoint.proceed()
            }
        }
    }
}

package nbcp.aop

import nbcp.db.EventResult
import org.slf4j.LoggerFactory
import java.io.Serializable
import java.lang.reflect.Method

/**
 * 通过日志打印方法的执行时间的切面
 *
 * @author Looly
 */
class SimpleTimeIntervalAspect : Aspect, Serializable {
    companion object {
        private var logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    private var startAt: Long = 0;
    override fun before(target: Any, method: Method, args: Array<Any?>): EventResult {
        startAt = System.currentTimeMillis();
        return EventResult()
    }

    override fun after(target: Any, method: Method, args: Array<Any?>, returnVal: Any?): EventResult {
        var msg = "[${target.javaClass.name}.${method.name}] execute spend [${System.currentTimeMillis() - startAt}]ms return value [${returnVal}]";
        logger.info(msg)
        return EventResult()
    }

    override fun afterException(target: Any, method: Method, args: Array<Any?>, e: Throwable): Boolean {
        return true;
    }
}

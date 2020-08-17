package nbcp.aop

import nbcp.db.EventResult
import java.lang.reflect.Method

/**
 * 切面接口
 *
 * @author looly
 * @author ted.L
 * @since 4.18
 */
interface Aspect {
    /**
     * 目标方法执行前的操作
     *
     * @param target 目标对象
     * @param method 目标方法
     * @param args   参数
     * @return 是否继续执行接下来的操作
     */
    fun before(target: Any, method: Method, args: Array<Any?>): EventResult

    /**
     * 目标方法执行后的操作
     * 如果 target.method 抛出异常且
     *
     * @param target    目标对象
     * @param method    目标方法
     * @param args      参数
     * @param returnVal 目标方法执行返回值
     */
    fun after(target: Any, method: Method, args: Array<Any?>, returnVal: Any?): EventResult

    /**
     * 目标方法抛出异常时的操作
     *
     * @param target 目标对象
     * @param method 目标方法
     * @param args   参数
     * @param e      异常
     * @return 是否允许抛出异常
     */
    fun afterException(target: Any, method: Method, args: Array<Any?>, e: Throwable): Boolean
}

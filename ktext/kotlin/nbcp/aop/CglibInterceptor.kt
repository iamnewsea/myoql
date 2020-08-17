package nbcp.aop

import net.sf.cglib.proxy.MethodInterceptor
import net.sf.cglib.proxy.MethodProxy
import java.io.Serializable
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * Cglib实现的动态代理切面
 *
 * @author looly, ted.L
 */
/**
 * 构造
 *
 * @param target 被代理对象
 * @param aspect 切面实现
 */
class CglibInterceptor(private val target: Any, private val aspect: Aspect) : MethodInterceptor, Serializable {

    override fun intercept(obj: Any, method: Method, args: Array<Any?>, proxy: MethodProxy): Any? {
        val target = target
        // 开始前回调
        var ev_result = aspect.before(target, method, args);
        if (ev_result.result == false) {
            return ev_result.extData;
        }

        var result: Any? = null
        try {
            result = proxy.invoke(target, args)
        } catch (e: InvocationTargetException) { // 异常回调（只捕获业务代码导致的异常，而非反射导致的异常）
            if (aspect.afterException(target, method, args, e.targetException)) {
                throw e
            }
        }

        // 结束执行回调
        ev_result = aspect.after(target, method, args, result)
        if (ev_result.result == false) {
            return ev_result.extData;
        }
        return result
    }
}
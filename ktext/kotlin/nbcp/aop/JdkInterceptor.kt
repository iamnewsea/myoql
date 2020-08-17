package nbcp.aop

import java.io.Serializable
import java.lang.reflect.InvocationHandler
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * JDK实现的动态代理切面
 *
 * @author Looly
 * @author ted.L
 */
/**
 * 构造
 *
 * @param target 被代理对象
 * @param aspect 切面实现
 */
class JdkInterceptor(val target: Any, private val aspect: Aspect) : InvocationHandler, Serializable {

    override fun invoke(proxy: Any, method: Method, args: Array<Any?>): Any? {
        val target = target
        val aspect = aspect
        // 开始前回调
        var ev_result = aspect.before(target, method, args)
        if (ev_result.result == false) {
            return ev_result.extData;
        }

        method.isAccessible = true;
        var result: Any? = null
        try {
            result = method.invoke(if (Modifier.isStatic(method.modifiers)) null else target, *args)
        } catch (e: InvocationTargetException) { // 异常回调（只捕获业务代码导致的异常，而非反射导致的异常）
            if (aspect.afterException(target, method, args, e.targetException)) {
                throw e
            }
        }

        ev_result = aspect.after(target, method, args, result)
        if (ev_result.result == false) {
            return ev_result.extData;
        }
        return result
    }

    companion object {
        private const val serialVersionUID = 1L
    }

}
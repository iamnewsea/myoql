package nbcp.base.utils

import java.lang.RuntimeException
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy

object ProxyUtil {

    /**
     * jdkProxy 只能生成接口类代理，且 invocation 不能直接调用方法，会造成自己调用自己的死循环
     */
    @JvmStatic
    fun <T : Any> jdkProxy(targetClass: Class<T>, invocation: InvocationHandler): T {
        if (targetClass.isInterface == false) {
            throw RuntimeException("必须是接口类")
        }

        return Proxy.newProxyInstance(
            targetClass.classLoader,
            arrayOf(targetClass),
            invocation
        ) as T
    }

}
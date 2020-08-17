package nbcp.utils

import nbcp.aop.Aspect
import nbcp.aop.CglibInterceptor
import nbcp.aop.JdkInterceptor
import net.sf.cglib.proxy.Enhancer
import java.lang.reflect.Proxy

//参考了：https://hutool.cn/docs/#/aop/%E5%88%87%E9%9D%A2%E4%BB%A3%E7%90%86%E5%B7%A5%E5%85%B7-ProxyUtil

object ProxyUtil {
    fun <T : Any> jdkProxy(target: T, aspect: Aspect): T {
        var type = target::class.java;
        return Proxy.newProxyInstance(
                type.classLoader,
                type.interfaces,
                JdkInterceptor(target, aspect)) as T
    }

    fun <T:Any> cglibProxy(target:T,aspect:Aspect):T{
        val enhancer = Enhancer()
        enhancer.setSuperclass(target.javaClass)
        enhancer.setCallback(CglibInterceptor(target, aspect))
        return enhancer.create() as T
    }
}
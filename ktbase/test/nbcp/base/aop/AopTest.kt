package nbcp.base.aop

import nbcp.base.TestBase
import nbcp.base.utils.ProxyUtil
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method


interface abc {
    fun hi(): String
}


@GroupLog("main")
class AopTest : TestBase() {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }


    @Test
    fun TestAop() {
        var obj = ProxyUtil.jdkProxy(abc::class.java, object : InvocationHandler {
            override fun invoke(proxy: Any, method: Method, args: Array<out Any>?): Any {
                println("invoke")
                if (method.name == "hi") {
                    return "hi"
                }
                return ""
            }
        });

        println(obj.hi())
    }

}
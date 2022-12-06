package nbcp.base.utils

import nbcp.base.TestBase
import nbcp.base.extend.AsLong
import nbcp.base.extend.AsString
import nbcp.base.extend.HasValue
import nbcp.base.model.InputStreamTextReaderThread
import org.junit.jupiter.api.Test
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import javax.script.ScriptEngineManager


class JsUtilTest : TestBase() {

    interface IWaiter {
        fun getGreeting(name: String): String;
    }

    @Test
    fun test_get() {
        var e = JavaScriptExecutor();
        var js = """ var obj= 'Hello, ' + name; """
        e.engine.put("name","udi")
        var result = e.evalAndGet(js, "obj").AsString();
        println(result)
    }

    @Test
    fun test_func() {
        var e = JavaScriptExecutor();
        var js = """ function getGreeting(name) { return 'Hello, ' + name; } """
        var result = e.evalFunction(js, "getGreeting", "udi").AsString();
        println(result)
    }

    /**
     * https://blog.csdn.net/qq_25255197/article/details/79072119
     */
    @Test
    fun test_interface() {
        var e = JavaScriptExecutor();
        var js = """ function getGreeting(name) { return 'Hello, ' + name; } """
        var waiter = e.evalAsInterface(js, IWaiter::class.java);
        var result = waiter.getGreeting("udi")
        println(result)
    }
    @Test
    fun test_method() {
        var e = JavaScriptExecutor();
        var js = """ var obj = { getGreeting : function(name) { return 'Hello, ' + name; } };  """
        var result = e.evalInvokeObjectMethod(js, "obj", "getGreeting", "udi");
        println(result)
    }

    @Test
    fun eval() {
        var d = JavaScriptExecutor();
        d.engine.put("a", 1)
        d.engine.put("b", 2)
        var shell = d.eval(
            """
var c = a+b;
print(c);
logger.info(c);
"""
        );

        println(shell)
    }
}
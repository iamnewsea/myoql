package nbcp.base.utils

import org.slf4j.LoggerFactory
import java.io.FileWriter
import javax.script.Invocable
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

class JavaScriptExecutor {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass);
    }

    val engine: ScriptEngine = ScriptEngineManager()
        .run {
            var ret = this.getEngineByName("JavaScript")
                ?: this.getEngineByName("nashorn")
                ?: this.getEngineByExtension("js");
//            ret.put("logger", logger)
            return@run ret;
        }

    fun setFileWriter(fileName: String) {
        engine.context.writer = FileWriter(fileName);
    }

    fun eval(content: String): Any? {
        return engine.eval(content);
    }

    fun evalAndGet(content: String, target: String): Any? {
        engine.eval(content);
        return engine.get(target);
    }


    /**
     * 调用Js的函数，返回结果
     */
    fun evalFunction(content: String, method: String, vararg args: String): Any? {
        engine.eval(content);
        val invocable = engine as Invocable;
        return invocable.invokeFunction(method, *args);
    }


    /**
     * 调用Js对象的函数，返回结果
     */
    fun evalInvokeObjectMethod(content: String, target: String, targetMethod: String, vararg args: String): Any {
        engine.eval(content);
        val invocable = engine as Invocable;
        val targetValue = engine.get(target);
        return invocable.invokeMethod(targetValue, targetMethod, *args);
    }

    /**
     *
     */
    fun <T> evalAsInterface(content: String, type: Class<T>): T {
        engine.eval(content);
        val invocable = engine as Invocable;
        return invocable.getInterface(type);
    }
}
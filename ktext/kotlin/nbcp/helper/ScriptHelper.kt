package nbcp.helper

import java.lang.RuntimeException
import javax.script.Compilable
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

enum class ScriptLanguageEnum {
    js,
    python,
    lua,
    groovy;


    companion object {
        private val scriptEngine: ScriptEngineManager by lazy {
            return@lazy ScriptEngineManager()
        }
    }

    fun getScriptEngine(): ScriptEngine {
        var name = this.toString();
        return scriptEngine.getEngineByName(name) ?: scriptEngine.getEngineByExtension(name)
        ?: scriptEngine.getEngineByMimeType(name) ?: throw RuntimeException("不支持脚本语言 ${name}")
    }


    fun execScript(script: String): Any? {
        var engine = getScriptEngine();
        if (engine is Compilable) {
            return engine.compile(script).eval();
        }

        return engine.eval(script);
    }
}
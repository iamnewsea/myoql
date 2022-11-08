package nbcp.base.enums

import nbcp.base.comm.*
import nbcp.base.extend.*
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
        val name = this.toString();
        return scriptEngine.getEngineByName(name) ?: scriptEngine.getEngineByExtension(name)
        ?: scriptEngine.getEngineByMimeType(name) ?: throw RuntimeException("不支持脚本语言 ${name}")
    }


    fun execScript(script: String): Any? {
        val engine = getScriptEngine();
        if (engine is Compilable) {
            return engine.compile(script).eval();
        }

        return engine.eval(script);
    }

    fun info(): String {
        val factory = getScriptEngine().factory
        return listOf(
            "Name: {name}",
            "Language name:{l_name}",
            "Language version:{version}",
            "Extensions:{extensions}",
            "Mime types:{types}",
            "Names:{names}",
        )
            .joinToString(const.line_break)
            .formatWithJson(
                StringMap(
                    "name" to factory.engineName,
                    "l_name" to factory.languageName,
                    "version" to factory.engineVersion,
                    "extensions" to factory.extensions.joinToString(","),
                    "types" to factory.mimeTypes.joinToString(","),
                    "names" to factory.names.joinToString(",")
                )
            )
    }
}
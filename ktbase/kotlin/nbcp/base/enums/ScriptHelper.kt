//package nbcp.base.enums
//
//import nbcp.base.comm.StringMap
//import nbcp.base.comm.const
//import nbcp.base.extend.formatWithJson
//import javax.script.Compilable
//import javax.script.ScriptEngine
//import javax.script.ScriptEngineManager
//
///**
// * 1. 默认只支持JavaScript, JDK17+需要额外引入 org.openjdk.nashorn:nashorn-core
// * 2. Python 需要引入： org.python:jython-standalone
// *
// */
//enum class ScriptLanguageEnum(val key:String,val  extension:String ) {
//    JAVA_SCRIPT("JavaScript","js"),
//    PYTHON("Python","py"),
//    LUA("Lua","lu"),
//    GROOVY("Groovy","go");
//
//
//    companion object {
//        private val scriptEngine: ScriptEngineManager by lazy {
//            return@lazy ScriptEngineManager()
//        }
//    }
//
//    fun getScriptEngine(): ScriptEngine {
//        return scriptEngine.getEngineByName(key) ?: scriptEngine.getEngineByExtension(key)
//        ?: scriptEngine.getEngineByMimeType(key) ?: throw RuntimeException("不支持脚本语言 ${key}")
//    }
//
//
//    fun execScript(script: String): Any? {
//        val engine = getScriptEngine();
//        if (engine is Compilable) {
//            return engine.compile(script).eval();
//        }
//
//        return engine.eval(script);
//    }
//
//    fun info(): String {
//        val factory = getScriptEngine().factory
//        return listOf(
//            "Name: {name}",
//            "Language name:{l_name}",
//            "Language version:{version}",
//            "Extensions:{extensions}",
//            "Mime types:{types}",
//            "Names:{names}",
//        )
//            .joinToString(const.line_break)
//            .formatWithJson(
//                StringMap(
//                    "name" to factory.engineName,
//                    "l_name" to factory.languageName,
//                    "version" to factory.engineVersion,
//                    "extensions" to factory.extensions.joinToString(","),
//                    "types" to factory.mimeTypes.joinToString(","),
//                    "names" to factory.names.joinToString(",")
//                )
//            )
//    }
//}
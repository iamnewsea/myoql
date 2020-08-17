package nbcp.utils

import nbcp.helper.ScriptLanguageEnum
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.Charset
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

/**
 * Created by udi on 17-4-21.
 */


object JsUtil {

    fun encodeURIComponent(value: String): String {
        return URLEncoder.encode(value, "utf-8");
    }


    fun decodeURIComponent(value: String): String {
        return URLDecoder.decode(value, "utf-8");
    }

    fun execScript(script: String): Any? {
        return ScriptLanguageEnum.js.execScript(script);
    }
}
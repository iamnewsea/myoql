package nbcp.utils

import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.Charset

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

}
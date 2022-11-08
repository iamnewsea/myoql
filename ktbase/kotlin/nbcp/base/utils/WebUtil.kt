package nbcp.base.utils


import nbcp.base.comm.*
import nbcp.base.extend.*

/**
 * Created by udi on 17-4-21.
 */


object WebUtil {
    /**
     * 判断是否是八进制类型：
     * 如果是 json,text,xml,form 则为普通类型。
     * 检测contentType就否包含以下内容：
    application
    audio
    font
    example
    image
    message
    model
    multipart
    video
     */
    @JvmStatic
    fun contentTypeIsOctetContent(contentType: String): Boolean {
        if (contentType.isEmpty()) return false;
        if (contentType.startsWith("text/")) return false


        if (contentType.startsWith("application/")) {
            if (contentType.contains("/vnd.")) return true;
            if (contentType.endsWith("+json")) return false
            if (contentType.endsWith("+xml")) return false
            if (contentType.endsWith("/javascript")) return false
            if (contentType.startsWith("application/json")) return false
            if (contentType.startsWith("application/xml")) return false
            if (contentType.startsWith("application/x-www-form-urlencoded")) return false
        }

        // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Type
        // https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Complete_list_of_MIME_types
        // https://www.iana.org/assignments/media-types/media-types.xhtml

        return true;
    }
    
    @JvmStatic
    fun fillUrlWithUserPassword(url: String, userName: String, password: String): String {
        var index = url.indexOf("//")
        if (index < 0) {
            throw java.lang.RuntimeException("${url} 不合法")
        }

        return url.insertAt(index + 2, "${JsUtil.encodeURIComponent(userName)}:${JsUtil.encodeURIComponent(password)}@")
    }
}
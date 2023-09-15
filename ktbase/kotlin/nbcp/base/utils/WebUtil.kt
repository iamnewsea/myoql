package nbcp.base.utils


import nbcp.base.comm.StringMap
import nbcp.base.extend.AsString
import nbcp.base.extend.getStringValue
import nbcp.base.extend.insertAt

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

        return url.insertAt(index + 2, "${UrlUtil.encodeURIComponent(userName)}:${UrlUtil.encodeURIComponent(password)}@")
    }



    @JvmStatic
    fun isLocalIp(Ip: String): Boolean {
        return Ip.isEmpty() || Ip.startsWith("127.") || Ip.startsWith("0.") || Ip.startsWith("0:")
    }


    /**
     * https://www.w3school.com.cn/media/media_mimeref.asp
     */
    private val mimeLists = StringMap(
            "css" to "text/css",
            "htm" to "text/html",
            "html" to "text/html",
            "js" to "application/javascript",
            "xml" to "text/xml",
            "gif" to "image/gif",
            "jpg" to "image/jpeg",
            "jpeg" to "image/jpeg",
            "png" to "image/jpeg",
            "tiff" to "image/tiff",
            "json" to "application/json",
            "txt" to "text/plain",
            "mp3" to "audio/mpeg",
            "avi" to "video/x-msvideo",
            "mp4" to "video/mpeg4",
            "doc" to "application/msword",
            "docx" to "application/msword",
            "pdf" to "application/pdf",
            "xls" to "application/vnd.ms-excel",
            "xlsx" to "application/vnd.ms-excel",
            "ppt" to "application/vnd.ms-powerpoint",
            "exe" to "application/octet-stream",
            "zip" to "application/zip",
            "m3u" to "audio/x-mpegurl",
            "svg" to "image/svg+xml",
            "h" to "text/plain",
            "c" to "text/plain",
            "dll" to "application/x-msdownload"
    )

    @JvmStatic
    fun getMimeType(extName: String): String {
        return mimeLists.getStringValue(extName.lowercase()).AsString()
    }


    /**
     * 补充 HttpHostUrl
     */
    @JvmStatic
    fun getFullHttpUrl(host: String): String {
        if (host.isEmpty()) return ""

        if (host.startsWith("http://", true) || host.startsWith("https://", true)) {
            return host;
        }

        if (host.startsWith("//")) {
            return "http:" + host;
        }

        return "http://" + host;
    }


    @JvmStatic
    fun getHostUrlWithoutHttp(host: String): String {
        if (host.isEmpty()) return ""

        if (host.startsWith("http://", true)) {
            return host.substring(5);
        }
        if (host.startsWith("https://", true)) {
            return host.substring(6);
        }

        if (host.startsWith("//")) {
            return host;
        }

        return "//" + host;
    }

}
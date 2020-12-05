@file:JvmName("MyMvcHelper")
@file:JvmMultifileClass

package nbcp.web

import nbcp.comm.*
import org.springframework.http.MediaType
import nbcp.utils.*
import org.springframework.web.servlet.HandlerMapping
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * 在中英环境下，返回多语言信息， 使用字典是比较麻烦的， 使用如下方式
 *
 * "服务器错误" lang "server error"
 */
infix fun String.lang(englishMessage: String): String {
    var lang = HttpContext.request.getAttribute("lang")?.toString() ?: "cn"
    if (lang == "en") return englishMessage;
    return this;
}

fun ServletResponse.WriteXmlRawValue(xml: String) {
    this.contentType = MediaType.TEXT_XML_VALUE;
    this.outputStream.write(xml.toByteArray(utf8));
}

fun ServletResponse.WriteJsonRawValue(json: String) {
    this.contentType = MediaType.APPLICATION_JSON_UTF8_VALUE;
    this.outputStream.write(json.toByteArray(utf8));
}

fun ServletResponse.WriteTextValue(text: String) {
    this.contentType = "text/html;charset=UTF-8";
    this.outputStream.write(text.toByteArray(utf8));
}

fun HttpServletResponse.setDownloadFileName(fileName: String) {
    this.setHeader("Content-Disposition", "attachment; filename=" + JsUtil.encodeURIComponent(fileName));
    this.contentType = "application/octet-stream"
}

private val String.ContentTypeIsOctetContent: Boolean
    get() {
        if (this.startsWith("text/")) return false
        if (this == "application/json") return false
        if (this == "application/xml") return false

        if (this.startsWith("application/")) {
            if (this.endsWith("+xml")) return false
            if (this.endsWith("+json")) return false
            if (this.startsWith("application/json")) return false
            if (this.startsWith("application/xml")) return false
            if (this.startsWith("application/x-www-form-urlencoded")) return false
        }

        // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Type
        // https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Complete_list_of_MIME_types
        // https://www.iana.org/assignments/media-types/media-types.xhtml


        for (c in arrayOf<String>("application/", "audio/", "font/", "example/", "image/", "message/", "model/", "multipart/", "video/")) {
            if (this.startsWith(c, 0, true)) return true;
        }
        return false;
    }

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
val HttpServletResponse.IsOctetContent: Boolean
    get() {
        if (this.contentType == null) {
            return true
        }

        return this.contentType.ContentTypeIsOctetContent
    }

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
val HttpServletRequest.IsOctetContent: Boolean
    get() {
        if (this.contentType == null) {
            return true
        }

        return this.contentType.ContentTypeIsOctetContent
    }


private fun _getClientIp(request: HttpServletRequest): String {
    // 如果 X-Real-IP == remoteAddr 且不是局域网Ip，则返回。
    var remoteAddr = request.remoteAddr
    var realIp = request.getHeader("X-Real-IP") ?: "";
    var forwardIps = (request.getHeader("X-Forwarded-For") ?: "")
            .split(",")
            .map { it.trim() }
            .filter { it.HasValue && !it.VbSame("unknown") && !MyUtil.isLocalIp(it) }
            .toList();


    if (MyUtil.isLocalIp(realIp)) {
        realIp = "";
    }

    //如果都没有设置，直接返回 remoteAddr.
    if (!forwardIps.any() || realIp.isEmpty()) {
        return remoteAddr
    }

    // 如果设置了 X-Real-IP
    // 必须 = realIp
    if (realIp.HasValue) {
        return realIp;
    }

    //如果设置了 X-Forwarded-For
    if (forwardIps.any()) {
        return forwardIps[0];
    }
    return remoteAddr
}

/**
 * 获取客户端Ip，已缓存到 Request 对象。
 * Nginx 应该在最外层， 设置 X-Real-IP 和 X-Forwarded-For.
 * proxy_set_header X-Real-IP $remote_addr;
 * proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
 * 如果没有设置 X-Real-IP 和 X-Forwarded-For，返回  remoteAddr
 * 否则返回 X-Real-IP ， X-Forwarded-For
 * 默认返回 remoteAddr
 */
val HttpServletRequest.ClientIp: String
    get() {
        var clientIp = this.getAttribute("ClientIp").AsString()
        if (clientIp.HasValue) {
            return clientIp
        }

        clientIp = _getClientIp(this);


        if (clientIp == "0:0:0:0:0:0:0:1") {
            clientIp = "127.0.0.1"
        }

        this.setAttribute("ClientIp", clientIp);
        return clientIp;
    }


/**
 * token前缀
 */
val tokenPrefix = "sf";

fun generateToken(): String {
    return tokenPrefix + CodeUtil.getCode();
}


/**
 * 把 queryString 加载为 Json
 */
val HttpServletRequest.queryJson: JsonMap
    get() {
        var queryJson_key = "_Request_Query_Json_"
        var dbValue = this.getAttribute(queryJson_key)
        if (dbValue != null) {
            return dbValue as JsonMap;
        }

        var dbValue2 = JsonMap.loadFromUrl(this.queryString ?: "")
        this.setAttribute(queryJson_key, dbValue2)
        return dbValue2;
    }

fun HttpServletRequest.findParameterStringValue(key: String): String {
    return this.findParameterValue(key).AsString()
}

fun HttpServletRequest.findParameterIntValue(key: String): Int {
    return this.findParameterValue(key).AsInt()
}


/**
 * 从request属性，Path变量， URL ， Form表单，Header 中查找参数
 */
fun HttpServletRequest.findParameterValue(key: String): Any? {
    var ret = this.getAttribute(key)
    if (ret != null) {
        return ret;
    }

    ret = (this.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE) as Map<String, Any?>?)?.get(key)
    if (ret != null) {
        return ret;
    }


    ret = this.queryJson.get(key)
    if (ret != null) {
        return ret;
    }

    if (this is MyHttpRequestWrapper) {
        ret = this.json.get(key)
        if (ret != null) {
            return ret;
        }
    }

    //读取表单内容
    if (this.contentType != null && this.contentType.startsWith(MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
        ret = this.getParameter(key)
        if (ret != null) {
            return ret;
        }
    }

    ret = this.getHeader(key)
    if (ret != null) {
        return ret;
    }

    return null;
}

/**
 * URL + 参数
 */
val HttpServletRequest.fullUrl: String
    get() {
        return this.requestURI + (if (this.queryString.isNullOrEmpty()) "" else ("?" + this.queryString))
    }

/**
 * 输出 javascript ，通过 window.parent.postMessage 函数呼叫父窗口，弹出消息 用于前端下载时处理消息。
 * 前端环境：
 *  1. 调用 jv.download() 函数，原理是通过页面的iframe,打开下载页面。
 *  2. 在主页面添加 window.addEventListener('message',e=>{}) 处理函数。
 * @param msg: 错误消息
 * @param title: 消息标题
 */
fun HttpServletResponse.parentAlert(msg: String, title: String = "", targetOrigin: String = "*") {
    /**
     * <pre>{@code
     * window.addEventListener('message',e=>{
     *      if( e.data.event == 'error') {
     *          jv.error.apply(jv, e.data.arguments)
     *      }
     *  });
     *  }
     *  </pre>
     */
    this.WriteTextValue("<script>window.parent.postMessage({event:'error',arguments:['${msg}','${title}']},'${targetOrigin}')</script>")
}
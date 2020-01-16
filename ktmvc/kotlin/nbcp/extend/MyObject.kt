package nbcp.web

import nbcp.comm.*
import nbcp.base.extend.*
import org.springframework.http.MediaType
import nbcp.base.utf8
import nbcp.base.utils.JsUtil
import nbcp.base.utils.MyUtil
import nbcp.db.LoginUserModel
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

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

        /**
        application
        audio
        font
        example
        image
        message
        model
        multipart
        text
        video
         */
        for (c in arrayOf<String>("application/", "audio/", "font/", "example/", "image/", "message/", "model/", "multipart/", "video/")) {
            if (this.startsWith(c, 0, true)) return true;
        }
        return false;
    }

val HttpServletResponse.IsOctetContent: Boolean
    get() {
        if (this.contentType == null) {
            return true
        }

        return this.contentType.ContentTypeIsOctetContent
    }

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


var Request_Id: UInt = 0U;

/**
 * 获取当前请求Id，已缓存。
 * Id值是自增的，不保证全周期唯一。
 */
val HttpServletRequest.RequestId: UInt
    get() {
        return this::RequestId::class.java.Lock {
            var ret = this.getAttribute("Request-Id");
            if (ret != null) {
                return@Lock ret.AsInt().toUInt();
            }

            ret = this.getHeader("Request-Id");
            if (ret != null) {
                this.setAttribute("Request-Id", ret);
                return ret.AsInt().toUInt();
            }
            /*LocalTime.now().toSecondOfDay().toUInt() + */
            var currentId = (++Request_Id);
            this.setAttribute("Request-Id", currentId);

            return@Lock currentId;
        }
    }

//var request_cache: IDataCache4Sql? = null
//var ServletRequest.requestCache: IDataCache4Sql?
//    get() {
//        return request_cache
//    }
//    set(value) {
//        request_cache = value
//    }

/**
 * 高并发系统不应该有Session。使用token即可。
 */
var HttpServletRequest.LoginUser: LoginUserModel
    get() {
        return this.getAttribute("(LoginUser)") as LoginUserModel? ?: LoginUserModel()
    }
    set(value) {
        this.setAttribute("(LoginUser)", value)
        HttpContext.response.setHeader("token", value.token)
    }


val HttpServletRequest.UserId: String
    get() {
        return this.LoginUser.id;
    }

val HttpServletRequest.LoginName: String
    get() {
        return this.LoginUser.loginName;
    }

val HttpServletRequest.UserName: String
    get() {
        return this.LoginUser.name;
    }

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

/**
 * 从request属性，Form表单， URL ， Header，Cookie中查找参数
 */
fun HttpServletRequest.findParameterValue(key: String): String? {
    var ret = this.getAttribute(key)?.toString()
    if (ret != null) {
        return ret;
    }

    ret = this.getParameter(key)
    if (ret != null) {
        return ret;
    }

    ret = this.queryJson.get(key)?.toString();
    if (ret != null) {
        return ret;
    }

    ret = this.getHeader(key)
    if (ret != null) {
        return ret;
    }

    ret = this.cookies?.firstOrNull { it.name == key }?.value
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

//呼叫父窗口，弹出消息
fun HttpServletResponse.parentAlert(msg: String, title: String = "", targetOrigin: String = "*") {
    this.WriteTextValue("<script>window.parent.postMessage({event:'error',arguments:['${msg}','${title}']},'${targetOrigin}')</script>")
}
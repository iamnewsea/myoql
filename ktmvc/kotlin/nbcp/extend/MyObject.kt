@file:JvmName("MyWebHelper")
@file:JvmMultifileClass

package nbcp.web

import nbcp.comm.*
import org.springframework.http.MediaType
import nbcp.utils.*
import nbcp.db.LoginUserModel
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


var Request_Id: UInt = 0U;

/**
 * 获取当前请求Id，已缓存。
 * Id值是自增的，不保证全周期唯一。
 */
//val HttpServletRequest.RequestId: UInt
//    get() {
//        return this::RequestId::class.java.Lock {
//            var ret = this.getAttribute("Request-Id");
//            if (ret != null) {
//                return@Lock ret.AsInt().toUInt();
//            }
//
//            ret = this.getHeader("Request-Id");
//            if (ret != null) {
//                this.setAttribute("Request-Id", ret);
//                return ret.AsInt().toUInt();
//            }
//            /*LocalTime.now().toSecondOfDay().toUInt() + */
//            var currentId = (++Request_Id);
//            this.setAttribute("Request-Id", currentId);
//
//            return@Lock currentId;
//        }
//    }

//var request_cache: IDataCache4Sql? = null
//var ServletRequest.requestCache: IDataCache4Sql?
//    get() {
//        return request_cache
//    }
//    set(value) {
//        request_cache = value
//    }

var getLoginUserFunc: ((HttpServletRequest) -> LoginUserModel?)? = null

/**
 * 高并发系统不应该有Session。使用token即可。
 * 设置 Attribute("[LoginUser]") ,需要通过 HandlerInterceptorAdapter.preHandle 通过 token 设置 LoginUser，来保持登录信息，比 RedisSession 简单
 */
var HttpServletRequest.LoginUser: LoginUserModel
    get() {
        return this.getAttribute("[LoginUser]") as LoginUserModel? ?: (getLoginUserFunc?.invoke(this) ?: LoginUserModel())
    }
    set(value) {
        this.setAttribute("[LoginUser]", value)
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

//    ret = this.cookies?.firstOrNull { it.name == key }?.value
//    if (ret != null) {
//        return ret;
//    }

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
 * 输出 javascript 呼叫父窗口，弹出消息 用于前端下载时处理消息。
 * 前端下载，
 * 前端使用： jv.download() 函数，在页面添加一个 iframe ,设置url 即可使用 ajax + open 的方式下载了。
 * 在出错的时候，通过 postMessage 呼叫父窗口弹出消息
 * @param msg: 错误消息
 * @param title: 消息标题
 */
fun HttpServletResponse.parentAlert(msg: String, title: String = "", targetOrigin: String = "*") {
    this.WriteTextValue("<script>window.parent.postMessage({event:'error',arguments:['${msg}','${title}']},'${targetOrigin}')</script>")
}
package nbcp.web

import nbcp.base.comm.JsonMap
import nbcp.base.extend.*
import org.springframework.http.MediaType
import nbcp.base.utf8
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

fun ServletResponse.WriteJsonRawValue(json: String) {
    this.contentType = MediaType.APPLICATION_JSON_UTF8_VALUE;
    this.outputStream.write(json.toByteArray(utf8));
}

fun ServletResponse.WriteTextValue(text: String) {
    this.contentType = "text/html;charset=UTF-8";
    this.outputStream.write(text.toByteArray(utf8));
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
            return false
        }

        return this.contentType.ContentTypeIsOctetContent
    }

val HttpServletRequest.IsOctetContent: Boolean
    get() {
        if (this.contentType == null) {
            return false
        }

        return this.contentType.ContentTypeIsOctetContent
    }


/**
 * 获取客户端Ip，已缓存到 Request 对象。
 * 1. 先从 X-Forwarded-For 中取第一个有效Ip。
 * 2. 如果取不到，则按顺序获取Ip："X-Real-IP", "Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"
 * 3. 最后取 remoteAddr
 */
val HttpServletRequest.ClientIp: String
    get() {
        var clientIp = this.getAttribute("ClientIp").AsString()
        if( clientIp.HasValue){
            return clientIp
        }

        clientIp = this.getHeader("X-Forwarded-For") ?: ""
        if (clientIp.VbSame("unknown")) {
            clientIp = "";
        }

        clientIp = clientIp.split(",").firstOrNull { o -> MyUtil.isLocalIp(o) == false } ?: ""

        if (clientIp.isEmpty() == false) {
            return clientIp;
        }

        var list = mutableListOf("X-Real-IP", "Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR")
//        if (list.contains(SystemContext.proxy_client_ip_header) == false) {
//            list.add(SystemContext.proxy_client_ip_header)
//        }

        if (list.any {
                    clientIp = this.getHeader(it) ?: "";
                    if (clientIp.VbSame("unknown")) {
                        clientIp = "";
                    }

                    if (clientIp.isEmpty() == false) {
                        return@any true;
                    }
                    return@any false
                }
        ) {
            return clientIp;
        }


        clientIp = this.remoteAddr

        if (clientIp == "0:0:0:0:0:0:0:1") {
            clientIp = "127.0.0.1"
        }

        this.setAttribute("ClientIp",clientIp);
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


var HttpServletRequest.LoginUser: LoginUserModel
    get() {
        return this.session.getAttribute("(LoginUser)") as LoginUserModel? ?: LoginUserModel()
    }
    set(value) {
        this.session.setAttribute("(LoginUser)", value)
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

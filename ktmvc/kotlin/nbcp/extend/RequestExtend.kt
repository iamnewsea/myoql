@file:JvmName("MyMvcHelper")
@file:JvmMultifileClass

package nbcp.web

import nbcp.comm.*
import org.springframework.http.MediaType
import nbcp.utils.*
import org.slf4j.LoggerFactory
import org.springframework.web.servlet.HandlerMapping
import javax.servlet.http.HttpServletRequest

internal val logger = LoggerFactory.getLogger("nbcp.web.MyMvcHelper")


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

        return WebUtil.contentTypeIsOctetContent(this.contentType);
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
 * 处理跨域。
 * 网关处理完跨域后，应该移除 origin
 */
fun HttpServletRequest.getCorsResponseMap(allowOrigins: List<String>): StringMap {
    var request = this;
    var requestOrigin = request.getHeader("origin") ?: ""
    if (requestOrigin.startsWith("http://", true)) {
        requestOrigin = requestOrigin.substring("http://".length)
    } else if (requestOrigin.startsWith("https://", true)) {
        requestOrigin = requestOrigin.substring("https://".length)
    }

    var retMap = StringMap();
    if (requestOrigin.isEmpty()) return retMap;

    var allow = allowOrigins.any { requestOrigin.contains(it) } ||
        requestOrigin.contains("localhost") ||
        requestOrigin.contains("127.0.0");

    if (allow == false) {
        logger.warn("系统忽略未允许的跨域请求源:${requestOrigin}")
        return retMap;
    }

    retMap.put("Access-Control-Allow-Origin", requestOrigin)
    retMap.put("Access-Control-Max-Age", "2592000") //30天。

    retMap.put("Access-Control-Allow-Credentials", "true")
    retMap.put("Access-Control-Allow-Methods", "GET,POST,PATCH,PUT,HEAD,OPTIONS,DELETE")


    var allowHeaders = mutableSetOf<String>();
    allowHeaders.add(config.tokenKey);
    //添加指定的
//    allowHeaders.add("Authorization")

    allowHeaders.addAll(
        request.getHeader("Access-Control-Request-Headers").AsString().split(",").filter { it.HasValue })
//    if (request.method == "OPTIONS") {    }
    //https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Headers/Access-Control-Expose-Headers

    if (allowHeaders.any()) {
        retMap.put("Access-Control-Allow-Headers", allowHeaders.joinToString(","))
        retMap.put("Access-Control-Expose-Headers", allowHeaders.joinToString(","))
    }
    return retMap;
}


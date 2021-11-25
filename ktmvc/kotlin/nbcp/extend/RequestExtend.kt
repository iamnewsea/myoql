@file:JvmName("MyMvcHelper")
@file:JvmMultifileClass

package nbcp.web

import nbcp.base.mvc.MyHttpRequestWrapper
import nbcp.comm.*
import org.springframework.http.MediaType
import nbcp.utils.*
import org.slf4j.LoggerFactory
import org.springframework.web.servlet.HandlerMapping
import java.lang.RuntimeException
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
        var queryJson_key = "[RequestQueryJson]"
        var dbValue = this.getAttribute(queryJson_key)
        if (dbValue != null) {
            return dbValue as JsonMap;
        }

        var dbValue2 = JsonMap.loadFromUrl(this.queryString ?: "")
        this.setAttribute(queryJson_key, dbValue2)
        return dbValue2;
    }

//文件上传或 大于 10 MB 会返回 null , throw RuntimeException("超过10MB不能获取Body!");
val HttpServletRequest.postBody: ByteArray?
    get() {
        var postBodyValue = this.getAttribute("[PostBody]");
        if (postBodyValue != null) {
            return postBodyValue as ByteArray?
        }


        //如果 10MB
        if (this.IsOctetContent) {
            return null;
        }
        if (this.contentLength > config.maxHttpPostSize.toBytes()) {
            throw RuntimeException("请求体超过${(config.maxHttpPostSize.toString()).AsInt()}!")
        }
        postBodyValue = this.inputStream.readBytes();
        this.setAttribute("[PostBody]", postBodyValue)

        return postBodyValue;
    }

private fun setValue(jm: JsonMap, prop: String, arykey: String, value: String) {
    if (arykey.isEmpty()) {
        jm[prop] = value;
        return;
    }

    var keyLastIndex = arykey.indexOf(']');
    var key = arykey.slice(1..keyLastIndex - 1);

    if (jm.containsKey(key) == false) {
        jm[key] = JsonMap();
    }

    setValue(jm[key] as JsonMap, key, arykey.substring(keyLastIndex + 1), value);
}

fun HttpServletRequest.getPostJson(): JsonMap {
    var contentType = this.contentType;

    if (contentType == null) {
        contentType = MediaType.APPLICATION_JSON_VALUE
    }

    if (contentType.startsWith(MediaType.APPLICATION_JSON_VALUE)) {
        val bodyString = (this.postBody ?: byteArrayOf()).toString(const.utf8).trim()

        if (bodyString.startsWith("{") && bodyString.endsWith("}")) {
            return bodyString.FromJsonWithDefaultValue();
        }

        throw RuntimeException("非法的Json")
    } else if (contentType.startsWith(MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
        //按 key进行分组，假设客户端是：
        // corp[id]=1&corp[name]=abc&role[id]=2&role[name]=def
        //会分成两组 ret["corp"] = json1 , ret["role"] = json2;
        //目前只支持两级。不支持  corp[role][id]
        if (this.parameterNames.hasMoreElements()) {
            val ret = JsonMap();
            for (key in this.parameterNames) {
                val value = this.getParameter(key);
                val keyLastIndex = key.indexOf('[');
                if (keyLastIndex >= 0) {
                    val mk = key.slice(0..keyLastIndex - 1);

                    setValue(ret, mk, key.substring(keyLastIndex), value);
                } else {
                    setValue(ret, key, "", value);
                }
            }
        } else {
            val bodyString = (this.postBody ?: byteArrayOf()).toString(const.utf8).trim()
            return JsonMap.loadFromUrl(bodyString)
        }
    }

    throw RuntimeException("不识别 content-type")
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


    ret = this.getPostJson().get(key)
    if (ret != null) {
        return ret;
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
fun HttpServletRequest.getCorsResponseMap(allowOrigins: List<String>, headers: List<String>): StringMap {
    //https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Headers/Access-Control-Expose-Headers

    var request = this;
    var requestOrigin_Ori = request.getHeader("origin") ?: ""
    var requestOrigin = requestOrigin_Ori
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
        logger.warn("系统忽略未允许的跨域请求源:${requestOrigin_Ori}")
        return retMap;
    }

    retMap.put("Access-Control-Allow-Origin", requestOrigin_Ori)
    retMap.put("Access-Control-Max-Age", "2592000") //30天。

    retMap.put("Access-Control-Allow-Credentials", "true")
    retMap.put("Access-Control-Allow-Methods", "GET,POST,PATCH,PUT,HEAD,OPTIONS,DELETE")


    var allowHeaders = mutableSetOf<String>();
    allowHeaders.add(config.tokenKey);
    allowHeaders.addAll(headers)
    //添加指定的
//    allowHeaders.add("Authorization")

    allowHeaders.addAll(
        request.getHeader("Access-Control-Request-Headers")
            .AsString()
            .split(",")
            .filter { it.HasValue }
    )

    allowHeaders.removeIf { it.isEmpty() }

    if (allowHeaders.any()) {
        retMap.put("Access-Control-Allow-Headers", allowHeaders.joinToString(","))
        retMap.put("Access-Control-Expose-Headers", allowHeaders.joinToString(","))
    }
    return retMap;
}


@file:JvmName("MyMvcHelper")
@file:JvmMultifileClass

package nbcp.mvc.mvc

import nbcp.base.comm.JsonMap
import nbcp.base.comm.StringMap
import nbcp.base.comm.config
import nbcp.base.comm.const
import nbcp.base.extend.*
import nbcp.base.utils.*
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.util.unit.DataSize
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest
import org.springframework.web.servlet.HandlerMapping
import java.io.InputStream
import javax.servlet.http.HttpServletRequest


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
        return WebUtil.contentTypeIsOctetContent(this.contentType.AsString());
    }


private fun _getClientIp(request: HttpServletRequest): String {
    /*
实际Header：
x-real-ip: 10.0.4.20
x-forwarded-for: 103.10.86.226,124.70.126.65,10.0.4.20
     */
    var forwardIps = (request.getHeader("X-Forwarded-For") ?: "")
            .split(",")
            .map { it.trim() }
            .filter { it.HasValue && !it.basicSame("unknown") && !WebUtil.isLocalIp(it) }
            .toList();

    //如果设置了 X-Forwarded-For
    if (forwardIps.any()) {
        return forwardIps[0];
    }

    var realIp = request.getHeader("X-Real-Ip") ?: "";
    // 如果设置了 X-Real-Ip
    // 必须 = realIp
    if (WebUtil.isLocalIp(realIp) == false) {
        return realIp;
    }

    var remoteAddr = request.remoteAddr
    return remoteAddr
}

/**
 * 获取客户端Ip，已缓存到 Request 对象。
 * Nginx 应该在最外层， 设置 X-Real-Ip 和 X-Forwarded-For.
 * proxy_set_header X-Real-Ip $remote_addr;
 * proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
 * 如果没有设置 X-Real-Ip 和 X-Forwarded-For，返回  remoteAddr
 * 否则返回 X-Real-Ip ， X-Forwarded-For
 * 默认返回 remoteAddr
 */
val HttpServletRequest.ClientIp: String
    get() {
        var clientIp = this.getAttribute("[ClientIp]").AsString()
        if (clientIp.HasValue) {
            return clientIp
        }

        clientIp = _getClientIp(this);


        if (clientIp == "0:0:0:0:0:0:0:1") {
            clientIp = "127.0.0.1"
        }

        this.setAttribute("[ClientIp]", clientIp);
        return clientIp;
    }


val HttpServletRequest.XTraceId: String
    get() {
        var xTraceId = getHeader("X-Trace_Id");
        if( xTraceId.HasValue){
            return xTraceId;
        }

        xTraceId = this.getAttribute("[XTraceId]").AsString()
        if (xTraceId.HasValue) {
            return xTraceId
        }

        xTraceId = CodeUtil.getCode();

        this.setAttribute("[XTraceId]", xTraceId);
        return xTraceId;
    }

/**
 * 把 queryString 加载为 Json
 */
val HttpServletRequest.queryJson: JsonMap
    get() {
        var queryJson_key = "[Request.QueryJson]"
        var dbValue = this.getAttribute(queryJson_key)
        if (dbValue != null) {
            return dbValue as JsonMap;
        }

        var dbValue2 = JsonMap.loadFromUrl(this.queryString ?: "")
        this.setAttribute(queryJson_key, dbValue2)
        return dbValue2;
    }

val HttpServletRequest.soloQueryKeys: Array<String>
    get() {
        return UrlUtil.getSoloKeysFromUrl(this.queryString ?: "")
    }

val HttpServletRequest.requestParameterKeys: Set<String>
    get() {
        return this.queryJson.keys + this.soloQueryKeys + this.getPostJson().keys
    }

//文件上传或 大于 10 MB 会返回 null , throw RuntimeException("超过10MB不能获取Body!");
val HttpServletRequest.postBody: ByteArray?
    get() {
        val postBody_key = "[Request.PostBody]"
        var postBodyValue = this.getAttribute(postBody_key);
        if (postBodyValue != null) {
            return postBodyValue as ByteArray?
        }


        //如果 10MB
        if (this.IsOctetContent) {
            return null;
        }

        var maxHttpPostSize = DataSize.parse(
                config.getConfig("server.servlet.max-http-post-size")
                        .AsString { config.getConfig("server.tomcat.max-http-post-size", "") }
//                .AsString{config.getConfig("server.tomcat.max-http-post-size", "")}
                        .AsString("2MB")
        )
        if (this.contentLength > maxHttpPostSize.toBytes()) {
            throw RuntimeException("请求体超过${(maxHttpPostSize.toString()).AsInt()}!")
        }
        postBodyValue = this.inputStream.readBytes();
        this.setAttribute(postBody_key, postBodyValue)

        return postBodyValue;
    }

private fun setValue(jm: MutableMap<String, Any?>, prop: String, arykey: String, value: String) {
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


private fun getPostJsonFromRequest(request: HttpServletRequest): JsonMap {

    var contentType = request.contentType;

    if (contentType.isNullOrEmpty()) {
        contentType = MediaType.APPLICATION_JSON_VALUE
    }

    if (contentType.startsWith(MediaType.APPLICATION_JSON_VALUE)) {
        val bodyString = (request.postBody ?: byteArrayOf()).toString(const.utf8).trim()

        if (bodyString.isEmpty()) {
            return JsonMap();
        }

        if (bodyString.startsWith("{") && bodyString.endsWith("}")) {
            return bodyString.FromJsonWithDefaultValue();
        }

        return JsonMap();
    } else if (contentType.startsWith(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            || contentType.startsWith("application/x-www-form-urlencode")
    ) {
        //按 key进行分组，假设客户端是：
        // corp[id]=1&corp[name]=abc&role[id]=2&role[name]=def
        //会分成两组 ret["corp"] = json1 , ret["role"] = json2;
        //目前只支持两级。不支持  corp[role][id]
        val ret = JsonMap();
        if (request.parameterNames.hasMoreElements()) {
            for (key in request.parameterNames) {
                val value = request.getParameter(key);
                val keyLastIndex = key.indexOf('[');
                if (keyLastIndex >= 0) {
                    val mk = key.slice(0..keyLastIndex - 1);

                    setValue(ret, mk, key.substring(keyLastIndex), value);
                } else {
                    setValue(ret, key, "", value);
                }
            }

            return ret;
        } else {
            val bodyString = (request.postBody ?: byteArrayOf()).toString(const.utf8).trim()
            return JsonMap.loadFromUrl(bodyString)
        }
    }

//    throw RuntimeException("不识别 content-type:${contentType}")
    return JsonMap();
}

fun HttpServletRequest.getPostJson(): JsonMap {
    val PostJson_key = "[Request.PostJson]"
    var postJsonValue = this.getAttribute(PostJson_key);
    if (postJsonValue != null) {
        return postJsonValue as JsonMap
    }

    var ret = getPostJsonFromRequest(this);

    this.setAttribute(PostJson_key, ret);
    return ret;
}


fun HttpServletRequest.findParameterStringValue(key: String): String {
    return this.findParameterValue(key).AsString()
}

fun HttpServletRequest.findParameterIntValue(key: String): Int {
    return this.findParameterValue(key).AsInt()
}

/**
 * 不转换Key，直接获取。
 */
fun HttpServletRequest.getParameterValue(key: String): Any? {
    var request = this;
    var ret = request.getAttribute(key)
    if (ret != null) {
        return ret;
    }

    ret = (request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE) as Map<String, Any?>?)?.get(key)
    if (ret != null) {
        return ret;
    }

    ret = request.queryJson.get(key)
    if (ret != null) {
        return ret;
    }


    var ret2 = request.parameterMap.get(key)
    if (ret2 != null && ret2.any()) {
        return ret2.joinToString(",");
    }

    ret = request.getPostJson().get(key)
    if (ret != null) {
        return ret;
    }


    //读取表单内容
    if (request.contentType != null &&
            (request.contentType.startsWith(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    || request.contentType.startsWith("application/x-www-form-urlencode"))
    ) {
        ret = request.getParameter(key)
        if (ret != null) {
            return ret;
        }
    }

    ret = request.getHeader(key)
    if (ret != null) {
        return ret;
    }

    return null;
}


/**
 * 从request属性，Path变量， URL ， Form表单，Header 中查找参数
 */
fun HttpServletRequest.findParameterValue(key: String): Any? {
    var ret = this.getParameterValue(key);
    if (ret != null) {
        return ret;
    }

    //兼容查询
    if (StringUtil.isKebabCase(key)) {
        var key2 = StringUtil.getSmallCamelCase(key);
        if (key != key2) {
            ret = this.getParameterValue(key2);
        }

        return ret;
    }

    var kKey = StringUtil.getKebabCase(key);
    if (kKey != key) {
        ret = this.getParameterValue(kKey);
    }

    return ret;
}

/**
 * URL + 参数
 */
val HttpServletRequest.fullUrl: String
    get() {
        return this.requestURI + (if (this.queryString.isNullOrEmpty()) "" else ("?" + this.queryString))
    }


fun StandardMultipartHttpServletRequest.getFirstUploadFileStream(): InputStream {
    return this
            .multiFileMap
            .values
            .first()
            .first()
            .inputStream
}


/**
 * 处理跨域。
 * 网关处理完跨域后，应该移除 origin
 */
fun HttpServletRequest.getCorsResponseMap(allowOrigins: List<String>, denyHeaders: List<String>): StringMap {
    //https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Headers/Access-Control-Expose-Headers

    var request = this;
    var requestOrigin = request.getHeader("origin") ?: ""


    var retMap = StringMap();
    if (requestOrigin.isEmpty()) return retMap;

    var allow = requestOrigin.contains("localhost") ||
            requestOrigin.startsWith("127.0.0") ||
            requestOrigin.startsWith("192.168.") ||
            allowOrigins.any { it == "*" || requestOrigin.contains(it) };

    if (allow == false) {
        LoggerFactory.getLogger("ktmvc.getCorsResponseMap").warn("系统忽略未允许的跨域请求源:${requestOrigin}")
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
            request.getHeader("Access-Control-Request-Headers")
                    .AsString()
                    .split(",")
                    .filter { it.HasValue }
    )

    allowHeaders.removeIf { it.isEmpty() }

//    allowHeaders = request.headerNames.toList().intersect(allowHeaders).toMutableSet()

    allowHeaders -= denyHeaders;

    if (allowHeaders.any()) {
        retMap.put("Access-Control-Allow-Headers", allowHeaders.joinToString(","))
        retMap.put("Access-Control-Expose-Headers", (allowHeaders + "Content-Disposition").joinToString(","))
    }
    return retMap;
}


fun HttpServletRequest.getCookie(name: String): String = this.cookies?.firstOrNull { it.name == name }?.value ?: ""

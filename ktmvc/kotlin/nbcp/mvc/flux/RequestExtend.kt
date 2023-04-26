@file:JvmName("MyFluxHelper")
@file:JvmMultifileClass

package nbcp.mvc.flux


import nbcp.base.comm.JsonMap
import nbcp.base.comm.StringMap
import nbcp.base.comm.config
import nbcp.base.extend.AsInt
import nbcp.base.extend.AsString
import nbcp.base.extend.HasValue
import nbcp.base.extend.basicSame
import nbcp.base.utils.MyUtil
import nbcp.base.utils.WebUtil
import org.slf4j.LoggerFactory
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.util.unit.DataSize
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono


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
val ServerHttpRequest.IsOctetContent: Boolean
    get() {
        return WebUtil.contentTypeIsOctetContent(this.headers.contentType.AsString());
    }


fun ServerHttpRequest.getHeader(key: String): String? {
    return this.headers.get(key)?.joinToString(",")
}

private fun _getClientIp(request: ServerHttpRequest): String {
    /*
实际Header：
x-real-ip: 10.0.4.20
x-forwarded-for: 103.10.86.226,124.70.126.65,10.0.4.20
     */
    var forwardIps = (request.getHeader("X-Forwarded-For") ?: "").split(",").map { it.trim() }
            .filter { it.HasValue && !it.basicSame("unknown") && !WebUtil.isLocalIp(it) }.toList();

    //如果设置了 X-Forwarded-For
    if (forwardIps.any()) {
        return forwardIps[0];
    }

    var realIp = request.getHeader("X-Real-IP") ?: "";
    // 如果设置了 X-Real-IP
    // 必须 = realIp
    if (WebUtil.isLocalIp(realIp) == false) {
        return realIp;
    }

    var remoteAddr = request.remoteAddress.address.hostAddress
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
val ServerWebExchange.ClientIp: String
    get() {
        var clientIp = this.attributes.get("[ClientIp]").AsString()
        if (clientIp.HasValue) {
            return clientIp
        }

        clientIp = _getClientIp(this.request);


        if (clientIp == "0:0:0:0:0:0:0:1") {
            clientIp = "127.0.0.1"
        }

        this.attributes.set("[ClientIp]", clientIp);
        return clientIp;
    }


/**
 * 把 queryString 加载为 Json
 */
val ServerHttpRequest.queryJson: JsonMap
    get() {
        return JsonMap(this.queryParams)
    }

//文件上传或 大于 10 MB 会返回 null , throw RuntimeException("超过10MB不能获取Body!");
val ServerWebExchange.postBody: Mono<ByteArray>
    get() {
//        val postBody_key = "[Request.PostBody]"
//        var postBodyValue = this.attributes.get(postBody_key) as ByteArray?;
//        if (postBodyValue != null) {
//            return postBodyValue
//        }


        //如果 10MB
        if (this.request.IsOctetContent) {
            return Mono.empty();
        }

        var maxHttpPostSize = DataSize.parse(
                config.getConfig("server.jetty.max-http-post-size")
//                .AsString { config.getConfig("server.tomcat.max-http-post-size", "") }
                        .AsString("2MB")
        )

        if (this.request.headers.contentLength > maxHttpPostSize.toBytes()) {
            throw RuntimeException("请求体超过${(maxHttpPostSize.toString()).AsInt()}!")
        }

        return this.request.body.collectList().map {
            val list = mutableListOf<Byte>()
            it.forEach { list.addAll(it.asInputStream().readBytes().toTypedArray()) }

            return@map list.toByteArray()
        }


//                .subscribe {
//                    postBodyValue = it.asInputStream().readBytes()
//
//                    LoggerFactory.getLogger("ktmvc.flux.request.extend").Important("postBodySize:" + postBodyValue?.size.AsString())
//                }

//        this.attributes.set(postBody_key, postBodyValue)

//        return postBodyValue;
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


//private fun getPostJsonFromRequest(swe: ServerWebExchange): JsonMap {
//
//    var request = swe.request;
//    var contentType = request.headers.contentType;
//
//    if (contentType == null) {
//        contentType = MediaType.APPLICATION_JSON
//    }
//
//    if (contentType.toString().startsWith(MediaType.APPLICATION_JSON_VALUE)) {
//        swe.postBody.subscribe {
//            val bodyString = it.toString(const.utf8).trim()
//
//            if (bodyString.isEmpty()) {
//                return@map "";
//            }
//
//            if (bodyString.startsWith("{") && bodyString.endsWith("}")) {
//                return@map bodyString.FromJsonWithDefaultValue();
//            }
//
//            return@map "";
//        }
//
//    } else if (contentType.toString().startsWith(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
//            || contentType.toString().startsWith("application/x-www-form-urlencode")) {
//
//        swe.postBody.map {
//            val bodyString = it.toString(const.utf8).trim()
//            return@map Mono.just(JsonMap.loadFromUrl(bodyString))
//        }
//
//    }
//
////    throw RuntimeException("不识别 content-type:${contentType}")
//    return Mono.empty();
//}

//fun ServerWebExchange.getPostJson(): JsonMap {
//    val PostJson_key = "[Request.PostJson]"
//    var postJsonValue = this.attributes.get(PostJson_key);
//    if (postJsonValue != null) {
//        return postJsonValue as JsonMap
//    }
//
//    var ret = getPostJsonFromRequest(this);
//
//    this.attributes.set(PostJson_key, ret);
//    return ret;
//}


fun ServerWebExchange.findParameterStringValue(key: String): String {
    return this.findParameterValue(key).AsString()
}

fun ServerWebExchange.findParameterIntValue(key: String): Int {
    return this.findParameterValue(key).AsInt()
}


/**
 * 从request属性，Path变量， URL ， Form表单，Header 中查找参数
 */
fun ServerWebExchange.findParameterValue(key: String): Any? {
    var ret = this.attributes.get(key)
    if (ret != null) {
        return ret;
    }

//    ret = (this.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE) as Map<String, Any?>?)?.get(key)
//    if (ret != null) {
//        return ret;
//    }


    ret = this.request.queryJson.get(key)
    if (ret != null) {
        return ret;
    }


//    ret = this.getPostJson().get(key)
//    if (ret != null) {
//        return ret;
//    }


    //读取表单内容
//    if (this.request.headers.contentType != null && this.request.headers.contentType.toString().startsWith(MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
//        ret = this.getParameter(key)
//        if (ret != null) {
//            return ret;
//        }
//    }

    ret = this.request.headers.get(key)
    if (ret != null) {
        return ret;
    }

    return null;
}

/**
 * URL + 参数
 */
val ServerHttpRequest.fullUrl: String
    get() {
        return this.uri.toString()
    }

/**
 * 处理跨域。
 * 网关处理完跨域后，应该移除 origin
 */
fun ServerHttpRequest.getCorsResponseMap(allowOrigins: List<String>, denyHeaders: List<String>): StringMap {
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
    allowHeaders.add("Content-Disposition");
    allowHeaders.add(config.tokenKey);
    //添加指定的
//    allowHeaders.add("Authorization")

    allowHeaders.addAll(request.getHeader("Access-Control-Request-Headers").AsString().split(",")
            .filter { it.HasValue })

    allowHeaders.removeIf { it.isEmpty() }

    allowHeaders -= denyHeaders;

    if (allowHeaders.any()) {
        retMap.put("Access-Control-Allow-Headers", allowHeaders.joinToString(","))
        retMap.put("Access-Control-Expose-Headers", (allowHeaders + "Content-Disposition").joinToString(","))
    }
    return retMap;
}


fun ServerHttpRequest.getCookie(name: String): String = this.cookies.getFirst(name)?.value ?: ""

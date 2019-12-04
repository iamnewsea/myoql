package nbcp.web.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import nbcp.base.extend.*
import nbcp.base.*

import nbcp.base.utils.CodeUtil
import nbcp.base.utils.Md5Util
import nbcp.web.*
import org.slf4j.MDC
import org.springframework.web.multipart.commons.CommonsMultipartResolver
import java.lang.Exception
import java.lang.reflect.UndeclaredThrowableException
import java.nio.charset.Charset
import java.util.concurrent.atomic.AtomicInteger
import javax.servlet.*
import javax.servlet.annotation.WebFilter
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Created by udi on 2017.3.11.
 * 不拦截 GET,HEAD,OPTIONS 请求
 * 需要配置 ：
 * 1. server.filter.allowOrigins
 * 2. server.filter.ignore-log-urls
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
//@Configuration
@WebFilter(urlPatterns = arrayOf("/*", "/**"))
//@WebFilter(urlPatterns = arrayOf("/**"), filterName = "MyAllFilter")
//@ConfigurationProperties(prefix = "nbcp.filter")
open class MyAllFilter : Filter {
    override fun destroy() {
        MDC.remove("request_id")
    }

    override fun init(p0: FilterConfig?) {
    }

    @Value("\${server.filter.allowOrigins:}")
    var allowOrigins: String = "";
    @Value("\${server.filter.ignore-log-urls:}")
    var ignoreLogUrls: List<String> = listOf()
//    @Value("\${server.session.cookie.name}")
//    var cookieName = "";

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    override fun doFilter(request: ServletRequest?, response: ServletResponse?, chain: FilterChain?) {
        if (!(request is HttpServletRequest) ||
//                FileExtentionInfo(request.requestURI).isStaticURI ||
                request.method == "HEAD" ||
                (request is MyHttpRequestWrapper)) {
            chain?.doFilter(request, response)
            return;
        }

        var httpRequest = request as HttpServletRequest
        var httpResponse = response as HttpServletResponse

        if (httpRequest.method == "OPTIONS") {
            procCORS(httpRequest, httpResponse)
            httpResponse.status = 200
            return;
        }

        MDC.put("request_id", httpRequest.RequestId.toString())
//        MDC.put("user_name", request.LoginUser.name.AsString())
//        MDC.put("client_ip", request.ClientIp)

        var ignoreLog = ignoreLogUrls.any {
            httpRequest.requestURI.startsWith(it, true) &&
                    !(it.last().isLetterOrDigit() && (httpRequest.requestURI.getOrNull(it.length)?.isLetterOrDigit()
                            ?: false))
        }

        if (ignoreLog) {
            using(LogScope.NoInfo) {
                next(httpRequest, httpResponse, chain);
            }
        } else {
            next(httpRequest, httpResponse, chain);
        }
    }

    fun next(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain?) {

//        var ctx = WebApplicationContextUtils.getWebApplicationContext(httpRequest.servletContext)
//        httpRequest.requestCache = ctx.getBean("request", IDataCache4Sql::class.java);
//        httpRequest.requestCache?.clear();

        var startAt = System.currentTimeMillis()

        //仅接收 c 端口的 token
//        var token = request.getHeader("token");
//        if (token.HasValue) {
//            //从 redis 中查 token
//            var data = SystemContext.getSessionData?.invoke(token, "sessionAttr:" + SystemContext.loginSessionKey)
//            if (data != null) {
//                request.session.setAttribute(SystemContext.loginSessionKey, data)
//            }
//        }


        var queryMap = request.queryJson

        if (request.method == "GET") {
            //JSONP
            if (queryMap.containsKey("callback")) {
                var myRequest = MyHttpRequestWrapper(request);
                var myResponse = MyHttpResponseWrapper(response)
                myResponse.characterEncoding = "utf-8";

                setLang(myRequest);

                chain?.doFilter(myRequest, myResponse);

                afterComplete(myRequest, myResponse, queryMap.getStringValue("callback"), startAt);
            } else {
                chain?.doFilter(request, response)

                logNewSession(request, response);
            }

            logger.info("${request.LoginUser.name.AsString()} ${request.ClientIp} ${request.method} ${request.requestURI}${if (request.queryString == null) "" else ("?" + request.queryString)}  --->\n" +
                    "[response] ${response.status} ${System.currentTimeMillis() - startAt}毫秒")


            return;
        }


        //如果是上传
        //        if (request.contentLength > 10485760) {
        //            chain?.doFilter(request, myResponse);
        //            return;
        //        }


        var myRequest = MyHttpRequestWrapper(request);
        var myResponse = MyHttpResponseWrapper(response);
        request.characterEncoding = "utf-8";

        procFilter(myRequest, myResponse, chain, startAt)
    }

    private fun logNewSession(request: HttpServletRequest, httpResponse: HttpServletResponse) {
        if (request.session.isNew) {
            var setCookie = httpResponse.getHeader("Set-Cookie");
            if (setCookie != null) {
                logger.info("Set-Cookie: ${setCookie}")
            }
        }
    }

    private fun procFilter(request: MyHttpRequestWrapper, response: MyHttpResponseWrapper, chain: FilterChain?, startAt: Long) {
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request, response))

        beforeRequest(request)

        //set lang
        setLang(request);

        var err: Throwable? = null
        try {
            chain?.doFilter(request, response);
        } catch (e: Exception) {
            var err = getInnerException(e);
            var errorInfo = mutableListOf<String>()
            errorInfo.add(err::class.java.simpleName + ": " + err.message.AsString())
            errorInfo.addAll(err.stackTrace.map { "\t" + it.className + "." + it.methodName + ": " + it.lineNumber }.take(24))

            logger.error(errorInfo.joinToString("\r\n"))
            response.status = 500;
            response.contentType = "application/json;charset=UTF-8"
            response.outputStream.write("""{"msg":${err.Detail.ToJsonValue()}}""".toByteArray(utf8))
        }

        procCORS(request, response)
        afterComplete(request, response, "", startAt);
    }

    private fun getInnerException(e: Throwable): Throwable {
        var err = e;
        if (err is UndeclaredThrowableException) {
            return err.undeclaredThrowable;
        }

        if (err is ServletException) {
            if (err.rootCause != null) {
                return getInnerException(err.rootCause)
            }
        }
        return err;
    }

    fun setLang(request: MyHttpRequestWrapper) {
        var lang = request.getCookie("lang");

        if (lang.isEmpty()) {
            var clientLang = request.getHeader("accept-language") ?: "";
            if (clientLang.isEmpty() || clientLang.indexOf("zh") >= 0) {
                lang = "zh"
            }
        }

        if (lang == "en") {
            request.setAttribute("lang", "en");
        } else {
            request.setAttribute("lang", "cn");
        }
    }

    private fun beforeRequest(request: MyHttpRequestWrapper) {
        if (logger.isInfoEnabled == false) return

        var msgs = mutableListOf<String>()
        msgs.add("[[[--------> ${request.LoginUser.name.AsString()} ${request.ClientIp} ${request.method} ${request.requestURI}" + (if (request.queryString == null) "" else ("?" + request.queryString)))

        if (request.headerNames.hasMoreElements()) {
            msgs.add("[request header]:")
        }

        for (h in request.headerNames) {
            msgs.add("\t${h}: ${request.getHeader(h)}")
        }


        var htmlString = (request.body ?: byteArrayOf()).toString(utf8)
        if (htmlString.HasValue) {
            msgs.add("[request body]:")
            msgs.add("\t" + htmlString)
        }


        logger.info(msgs.joinToString(line_break))
    }

    private fun procCORS(request: HttpServletRequest, response: HttpServletResponse) {
        var originClient = request.getHeader("origin") ?: ""

        if (originClient.isEmpty()) return

        var allowOrigins = this.allowOrigins.split(",") //非 localhost 域名

        var allow = allowOrigins.any { originClient.contains(it) } ||
                originClient.contains("localhost") ||
                originClient.contains("127.0.0.1");

        if (allow) {
            response.setHeader("Access-Control-Allow-Origin", originClient)
            response.setHeader("Access-Control-Max-Age", "2592000") //30天。

            response.setHeader("Access-Control-Allow-Credentials", "true")
            response.setHeader("Access-Control-Allow-Methods", request.getHeader("Access-Control-Request-Method"))


            var allowHeaders = mutableListOf<String>();

            if (request.method == "OPTIONS") {
                allowHeaders.addAll(request.getHeader("Access-Control-Request-Headers").AsString().split(",").filter { it.HasValue })
            }

            if (allowHeaders.any() == false) {
                allowHeaders = request.headerNames.toList().toMutableList()

                //https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Headers/Access-Control-Expose-Headers
                var standardHeaders = arrayOf(
                        "expires",
                        "cache-control",
                        "content-language",
                        "content-type",
                        "last-modified",
                        "pragma",
                        "origin",
                        "accept",
                        "user-agent",
                        "connection",
                        "host",
                        "accept-language",
                        "accept-encoding"
                )
                //移除标准 header
                allowHeaders.removeAll { standardHeaders.contains(it.toLowerCase()) }
            }

            if (allowHeaders.any()) {
                response.setHeader("Access-Control-Allow-Headers", allowHeaders.joinToString(","))
            }
            response.setHeader("Access-Control-Expose-Headers", "*")
        }
    }

    fun afterComplete(request: MyHttpRequestWrapper, response: MyHttpResponseWrapper, callback: String, startAt: Long) {
        if (response.IsOctetContent) {
            var msg = mutableListOf<String>()
            msg.add("[response] ${request.requestURI} ${response.status} ${System.currentTimeMillis() - startAt}毫秒")

            for (h in response.headerNames) {
                msg.add("\t${h}:${response.getHeader(h)}")
            }
            msg.add("<----]]]");
            logger.info(msg.joinToString(line_break))
            return;
        }

        //设置 Set-Cookie:PZXTK=59160c3a-5443-490f-a94f-db1e83f041fd; Path=/; HttpOnly
//        var setCookieValue = myResponse.getHeader("Set-Cookie")
//        if (setCookieValue.HasValue) {
//            myResponse.setHeader("Set-Cookie", setCookieValue.replace("HttpOnly", "").trim().trimEnd(';'))
//
//            var scv = setCookieValue.split(";").filter { it.startsWith("Set-Cookie") }.firstOrNull() ?: ""
//            if (scv.HasValue) {
//                setCookieValue = scv.split(":").last().split("=").last().trim();
//
//                myResponse.addHeader("sessionId", setCookieValue)
//            }
//        }


        var resValue = response.result;
        var resStringValue = ""
        if (resValue != null) {
            resStringValue = resValue.toString(utf8)

            if (response.status < 400 && resValue.size > 32) {
                var md5 = Md5Util.getBase64Md5(resValue);
                //body id
                response.addHeader("_bid_", md5);

                var ori_md5 = request.getHeader("_bid_");
                if (ori_md5.HasValue) {
                    if (md5 == Md5Util.getBase64Md5(resValue)) {
                        response.status = 280
                    }
                }
            }
        }

        if (callback.isNotEmpty() && response.contentType.contains("json")) {
            response.contentType = "application/javascript;charset=UTF-8"
            response.result = """${callback}(${resStringValue})""".toByteArray(utf8)
        } else if (response.status == 280) {
            response.result = byteArrayOf();
        } else {
            response.result = resValue
        }

        if (logger.isInfoEnabled) {
            var msg = mutableListOf<String>()
            msg.add("[response] ${request.requestURI} ${response.status} ${System.currentTimeMillis() - startAt}毫秒")

            for (h in response.headerNames) {
                msg.add("\t${h}:${response.getHeader(h)}")
            }

            if (resValue != null && resValue.size > 0) {
                msg.add("[response body]:")
                msg.add("\t" + resStringValue.Slice(0, 8192))
            }

            msg.add("<----]]]")
            logger.info(msg.joinToString(line_break))
        }
    }
}
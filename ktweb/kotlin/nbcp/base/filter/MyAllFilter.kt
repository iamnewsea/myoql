package nbcp.base.filter

import ch.qos.logback.classic.Level
import nbcp.base.mvc.HttpContext
import nbcp.base.mvc.MyHttpRequestWrapper
import nbcp.base.mvc.MyHttpResponseWrapper
import nbcp.comm.*
import nbcp.utils.*
import nbcp.web.*
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.lang.reflect.UndeclaredThrowableException
import java.time.LocalDateTime
import javax.servlet.*
import javax.servlet.annotation.WebFilter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Created by udi on 2017.3.11.
 * 拦截所有请求，过滤普通的GET及上传下载。
 * 需要配置 ：
 * 1. app.filter.allow-origins
 * 2. app.filter.headers
 * 3. 通过 Url参数 log-level 控制 Log级别,可以是数字，也可以是被 ch.qos.logback.classic.Level.toLevel识别的参数，不区分大小写，如：all|trace|debug|info|error|off
 */
@WebFilter(urlPatterns = ["/*", "/**"])
//@WebFilter(urlPatterns = arrayOf("/**"), filterName = "MyAllFilter")
//@ConfigurationProperties(prefix = "nbcp.filter")
open class MyAllFilter : Filter {
    override fun destroy() {
        MDC.remove("request_id")
    }

    override fun init(p0: FilterConfig?) {
    }

    @Value("\${app.filter.ignore-log-urls:/health}")
    var ignoreLogUrls: List<String> = listOf()

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    override fun doFilter(request: ServletRequest?, response: ServletResponse?, chain: FilterChain?) {
        var httpRequest = request as HttpServletRequest
        var httpResponse = response as HttpServletResponse

        var request_id = httpRequest.tokenValue;

        MDC.put("request_id", request_id)

        var logLevel = getLogLevel(httpRequest);

        if (logLevel != null) {
            usingScope(logLevel) {
                next(httpRequest, httpResponse, chain);
            }
        } else {
            next(httpRequest, httpResponse, chain);
        }
    }

    private fun getLogLevel(httpRequest: HttpServletRequest): LogLevelScope? {
        var logLevel: Level? = null;

        var logLevelString = httpRequest.queryJson.get("-log-level-").AsString();
        if (logLevelString.HasValue &&
            config.adminToken == httpRequest.findParameterStringValue("-admin-token-")
        ) {
            if (logLevelString.IsNumberic()) {
                var logLevelInt = logLevelString.AsInt()
                if (logLevelInt > 0) {
                    logLevel = Level.toLevel(logLevelInt, Level.WARN)
                }
            } else {
                logLevel = Level.toLevel(logLevelString, Level.WARN)
            }
        } else {
            var ignoreLog = ignoreLogUrls.any {
                var url = it;
                var exact = url.startsWith("(") && url.endsWith(")")
                if (exact) {
                    url = url.Slice(1, -1);
                }

                if (url.startsWith("/") == false) {
                    url = "/" + url;
                }
                if (exact) {
                    return@any httpRequest.requestURI.startsWith(url, true)
                } else {
                    return@any httpRequest.requestURI.equals(url, true)
                }
            }

            if (ignoreLog) {
                logLevel = Level.OFF;
            }
        }

        if (logLevel == null) return null;
        return logLevel.levelInt.ToEnum<LogLevelScope>()
    }

    fun next(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain?) {

        if (request.method == "GET") {
            //JSONP
            proc_get(request, response, chain);
            return;
        }


        //如果是上传
        //        if (request.contentLength > 10485760) {
        //            chain?.doFilter(request, myResponse);
        //            return;
        //        }


        procFilter(request, response, chain)
    }

    private fun proc_get(_request: HttpServletRequest, _response: HttpServletResponse, chain: FilterChain?) {
        var startAt = LocalDateTime.now()


        var queryMap = _request.queryJson

        //JSONP
        if (queryMap.containsKey("callback")) {
            var request = MyHttpRequestWrapper.create(_request);
            var response = MyHttpResponseWrapper.create(_response)

            response.characterEncoding = "utf-8";

            setLang(request);

            RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request, response))
            beforeRequest(request);
            try {
                HttpContext.init(request, response);
                chain?.doFilter(request, response);
            } catch (e: Exception) {
                logger.Error {
                    var msgs = mutableListOf<String>()
                    msgs.add("[[----> ${request.tokenValue} ${request.ClientIp} ${request.method} ${request.fullUrl}")
                    msgs.add(e.message ?: "服务器错误");
                    msgs.add("<----]]")

                    return@Error msgs.joinToString(const.line_break)
                }

                if (request.findParameterStringValue("-iniframe-").AsBoolean()) {
                    response.parentAlert(e.message ?: "服务器错误")
                } else {
                    response.WriteTextValue(e.message ?: "服务器错误")
                }
                return;
            }
            afterComplete(request, response, queryMap.getStringValue("callback").AsString(), startAt, "");
        } else {
            //保持原始对象。
            var request = _request;
            var response = _response;

            try {
                HttpContext.init(request, response);
                chain?.doFilter(request, response)
            } catch (e: Exception) {
                logger.Error {
                    var msgs = mutableListOf<String>()
                    msgs.add("[[----> ${request.tokenValue} ${request.ClientIp} ${request.method} ${request.fullUrl}")
                    msgs.add(e.message ?: "服务器错误");
                    msgs.add("<----]]")
                    return@Error msgs.joinToString(const.line_break)
                }
                if (request.findParameterStringValue("-iniframe-").AsBoolean()) {
                    response.parentAlert(e.message ?: "服务器错误")
                } else {
                    response.WriteTextValue(e.message ?: "服务器错误")
                }
                return;
            }
        }


        var endAt = LocalDateTime.now()

        logger.Info {
            var request = _request;
            var response = _response;

            var msgs = mutableListOf<String>()
            msgs.add("[[----> ${request.tokenValue} ${request.ClientIp} ${request.method} ${request.fullUrl}")
            msgs.add("[response] ${response.status} ${endAt - startAt}")


            for (h in response.headerNames) {
                var key = h;
                var headerValues = response.getHeaders(h);
                if (headerValues.size > 1) {
                    key = "[${key}]"
                }

                msgs.add("\t${key}:${headerValues.joinToString(",")}")
            }

            msgs.add("<----]]")
            return@Info msgs.joinToString(const.line_break)
        }

    }


    private fun procFilter(
        _request: HttpServletRequest,
        _response: HttpServletResponse,
        chain: FilterChain?
    ) {
        var request = MyHttpRequestWrapper.create(_request);
        var response = MyHttpResponseWrapper.create(_response);
        request.characterEncoding = "utf-8";

        var startAt = LocalDateTime.now()

        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request, response))

        beforeRequest(request)

        //set lang
        setLang(request);
        var errorMsg = ""

        try {

            HttpContext.init(request, response);
            chain?.doFilter(request, response);
        } catch (e: Exception) {
            logger.Error {
                var err = getInnerException(e);
                errorMsg = err.Detail.AsString(err.message.AsString()).AsString("(未知错误)")
                var errorInfo = mutableListOf<String>()
                errorInfo.add(err::class.java.simpleName + ": " + errorMsg)
                errorInfo.addAll(err.stackTrace.map { "\t" + it.className + "." + it.methodName + ": " + it.lineNumber }
                    .take(24))

                return@Error errorInfo.joinToString(const.line_break)
            }

            errorMsg = JsonMap("msg" to errorMsg).ToJson()
            response.status = 500;
        }

        afterComplete(request, response, "", startAt, errorMsg);
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
        logger.Info {
            var msgs = mutableListOf<String>()
            msgs.add("[[----> ${request.tokenValue} ${request.ClientIp} ${request.method} ${request.fullUrl}")

            if (request.headerNames.hasMoreElements()) {
                msgs.add("[request header]:")
            }

            for (h in request.headerNames) {
                msgs.add("\t${h}: ${request.getHeader(h)}")
            }


            var htmlString = (request.body ?: byteArrayOf()).toString(const.utf8)
            if (htmlString.HasValue) {
                msgs.add("[request body]:")
                msgs.add("\t" + htmlString)
            }


            return@Info msgs.joinToString(const.line_break)
        }
    }


    fun afterComplete(
            request: MyHttpRequestWrapper,
            response: MyHttpResponseWrapper,
            callback: String,
            startAt: LocalDateTime,
            errorMsg: String
    ) {
        var error = errorMsg.HasValue;
        var resStringValue = errorMsg;
        if (error) {
            response.contentType = "application/json;charset=UTF-8"
            response.result = resStringValue.toByteArray(const.utf8)
        } else if (response.IsOctetContent == false) {
            var resValue = response.result ?: byteArrayOf();
            resStringValue = resValue.toString(const.utf8);

            if (callback.isNotEmpty() && response.contentType.contains("json")) {
                response.contentType = "application/javascript;charset=UTF-8"
                response.result = """${callback}(${resStringValue})""".toByteArray(const.utf8)
            } else {
                if (response.status == 280) {
                    response.result = byteArrayOf();
                } else {
                    response.result = resValue
                }
            }
        }

        val endAt = LocalDateTime.now();
        logger.Info {
            val msg = mutableListOf<String>()
            msg.add("[response] ${request.requestURI} ${response.status} ${endAt - startAt}")

            for (h in response.headerNames) {
                msg.add("\t${h}:${response.getHeader(h)}")
            }

            if (resStringValue.HasValue) {
                msg.add("[response body]:")
                val logResLength = request.queryJson.get("-log-res-length-").AsInt(1024);
                var subLen = logResLength / 2;

                if (resStringValue.length > logResLength) {
                    msg.add("\t" + resStringValue.substring(0, subLen) + "\n〘…〙\n" + resStringValue.Slice(-subLen))
                } else {
                    msg.add("\t" + resStringValue)
                }
            }

            msg.add("<----]]")
            return@Info msg.joinToString(const.line_break)
        }
    }
}
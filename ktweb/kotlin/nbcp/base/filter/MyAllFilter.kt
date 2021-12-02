package nbcp.base.filter

import ch.qos.logback.classic.Level
import nbcp.base.mvc.HttpContext
import nbcp.base.mvc.MyHttpRequestWrapper
import nbcp.comm.*
import nbcp.utils.*
import nbcp.web.*
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.logging.LogLevel
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
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
@Component
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
        private val UseClientCacheDataStatus = 280;
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
        fun getRequestWrapper(servletRequest: HttpServletRequest): MyHttpRequestWrapper {
            return MyHttpRequestWrapper.create(servletRequest);
        }

        fun getResponseWrapper(servletResponse: HttpServletResponse): ContentCachingResponseWrapper {
            if (servletResponse is ContentCachingResponseWrapper) {
                return servletResponse
            }
            return ContentCachingResponseWrapper(servletResponse);
        }
    }

    override fun doFilter(request: ServletRequest?, response: ServletResponse?, chain: FilterChain?) {
        var request = getRequestWrapper(request as HttpServletRequest);
        var response = getResponseWrapper(response as HttpServletResponse);

        request.characterEncoding = "utf-8";

        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request, response))
        HttpContext.init(request, response);

        //set lang
        setLang(request)


        var request_id = request.tokenValue;
        MDC.put("request_id", request_id)
        var logLevel = getLogLevel(request);

        if (logLevel != null) {
            if (logLevel == LogLevelScope.off) {
                chain?.doFilter(request, response)
                return;
            }

            usingScope(logLevel) {
                procFilter(request, response, chain);
            }
            return;
        }

        procFilter(request, response, chain);
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


    private fun procFilter(
        request: MyHttpRequestWrapper,
        response: ContentCachingResponseWrapper,
        chain: FilterChain?
    ) {
        var startAt = LocalDateTime.now()
        beforeRequest(request)
        var errorMsg = ""
        try {
            chain?.doFilter(request, response);
        } catch (e: Exception) {
            var err = getInnerException(e);
            var errorInfo = mutableListOf<String>()
            errorInfo.add(
                err::class.java.simpleName + ": " + err.Detail.AsString(err.message.AsString()).AsString("(未知错误)")
            )

            errorInfo.addAll(err.stackTrace.map { "\t" + it.className + "." + it.methodName + ": " + it.lineNumber }
                .take(24))

            errorMsg = errorInfo.joinToString(const.line_break)
            response.status = 500;
        } finally {
            var callback = "";
//            if (request.method == "GET") {
//                callback = request.queryJson.getStringValue("callback").AsString();
//            }
            afterComplete(request, response, callback, startAt, errorMsg);
        }
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

    fun setLang(request: ContentCachingRequestWrapper) {
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

    private fun beforeRequest(request: ContentCachingRequestWrapper) {
        logger.Important("--> ${request.tokenValue} ${request.ClientIp} ${request.method} ${request.fullUrl}")
    }


    fun afterComplete(
        request: HttpServletRequest,
        response: ContentCachingResponseWrapper,
        callback: String,
        startAt: LocalDateTime,
        errorMsg: String
    ) {
        var hasError = errorMsg.HasValue;
        var resStringValue = errorMsg;
        if (hasError) {
            response.contentType = "application/json;charset=UTF-8"
            val content = resStringValue.toByteArray(const.utf8);

            //重设输出。
            response.reset();
            response.setContentLength(content.size)
            response.outputStream.use {
                it.write(content)
                it.flush()
            }
        } else if (response.status == 204) {
        } else if (response.IsOctetContent) {
        } else {
            if (callback.isNotEmpty() && response.contentType.contains("json")) {
                resStringValue = response.contentAsByteArray.toString(const.utf8);
                response.contentType = "application/javascript;charset=UTF-8"
                val content = """${callback}(${resStringValue})""".toByteArray(const.utf8)

                response.reset()
                response.setContentLength(content.size)
                response.outputStream.use {
                    it.write(content)
                    it.flush()
                }
            }
        }

        //写入到输出流
        response.copyBodyToResponse()

        val endAt = LocalDateTime.now();
        logger.InfoError(hasError) {
            var msgs = mutableListOf<String>()
            msgs.add("[--> ${request.tokenValue} ${request.ClientIp} [${request.method}] ${request.fullUrl}")

            for (h in request.headerNames) {
                msgs.add("\t${h}: ${request.getHeader(h)}")
            }

            var htmlString = (request.postBody ?: byteArrayOf()).toString(const.utf8)
            if (htmlString.HasValue) {
                msgs.add("[request body]:")
                msgs.add("\t" + htmlString)
            }


            msgs.add("[response] ${response.status} ${endAt - startAt}")

            for (h in response.headerNames) {
                msgs.add("\t${h}:${response.getHeader(h)}")
            }

            if (resStringValue.HasValue) {
                msgs.add("[response body]:")
                val logResLength = request.queryJson.get("-log-res-length-").AsInt(1024);
                var subLen = logResLength / 2;

                if (resStringValue.length > logResLength) {
                    msgs.add("\t" + resStringValue.substring(0, subLen) + "\n〘…〙\n" + resStringValue.Slice(-subLen))
                } else {
                    msgs.add("\t" + resStringValue)
                }
            }

            msgs.add("<--]")
            return@InfoError msgs.joinToString(const.line_break)
        }
    }
}
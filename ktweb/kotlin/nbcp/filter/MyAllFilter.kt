package nbcp.filter

import ch.qos.logback.classic.Level
import nbcp.comm.*


import nbcp.utils.*
import nbcp.comm.JsonMap
import nbcp.comm.*
import nbcp.web.*
import org.reflections.Reflections
import org.reflections.util.ClasspathHelper
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.DependsOn
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.http.MediaType
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.io.File
import java.lang.reflect.UndeclaredThrowableException
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.jar.JarFile
import javax.servlet.*
import javax.servlet.annotation.WebFilter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Created by udi on 2017.3.11.
 * 拦截所有请求，过滤普通的GET及上传下载。
 * 需要配置 ：
 * 0. 标注 @SpringBootApplication 的启动类，还需要添加 @ServletComponentScan 注解。
 *    如果不是 nbcp包,以下两种方法任选一种。
 *        A) @ServletComponentScan(value = {"nbcp.**"})）。
 *        B) @Import({SpringUtil.class, MyAllFilter.class})
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

//    @Value("\${app.filter.allow-origins:}")
//    var allowOrigins: String = "";

//    @Value("\${app.filter.headers:token,Authorization}")
//    var headers: List<String> = listOf()

    /**
     * 静态文件在 resources 里的文件夹。前后不能有 "/"
     */
    @Value("\${app.filter.html-path:public}")
    var htmlPath: String = "public"

    @Value("\${app.filter.ignore-log-urls:/health}")
    var ignoreLogUrls: List<String> = listOf()

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)

        /**
         * 以public开头的（以"/"开头） 文件夹下的静态资源文件，不包括目录
         */
//        @JvmStatic
//        val htmlFiles:Set<String> by lazy{
//
//            var htmlPath = System.getProperty("app.filter.html-path").AsString("public")
//            htmlFiles = ClasspathHelper.forResource("${htmlPath}/").map { it.path }.toMutableSet()
//
//            //
//            htmlIndexFiles.forEach { htmlIndexFile ->
//                htmlFiles.filter { file -> file.endsWith("/" + htmlIndexFile, true) }.forEach { indexFile ->
//                    var lastIndex = indexFile.lastIndexOf("/");
//                    if (lastIndex >= 0) {
//                        htmlPaths.set(indexFile.substring(0, lastIndex), htmlIndexFile);
//                    }
//                }
//            }
//
//
//            return@lazy
//        }
//
//        /**
//         * 以public开头的（以"/"开头） 文件夹下目录，以 "/" 结尾
//         */
//        @JvmStatic
//        var htmlPaths = mutableMapOf<String, String>()
//
//        @JvmStatic
//        var htmlIndexFiles = setOf("index.html", "index.htm", "home.html", "home.htm", "default.html", "default.htm");
    }

    override fun doFilter(request: ServletRequest?, response: ServletResponse?, chain: FilterChain?) {
        var httpRequest = request as HttpServletRequest
        var httpResponse = response as HttpServletResponse

        HttpContext.init(httpRequest, httpResponse);

        if (request is HttpServletRequest == false) {
            chain?.doFilter(request, response)
            return;
        }

//        httpRequest.getCorsResponseMap(this.allowOrigins.split(",")).forEach { key, value ->
//            httpResponse.setHeader(key, value);
//        }
//
//
//        if (httpRequest.method == "OPTIONS") {
//            httpResponse.status = 204
//            return;
//        }

        var request_id = httpRequest.tokenValue;

        MDC.put("request_id", request_id)
//        MDC.put("user_name", request.LoginUser.name.AsString())
//        MDC.put("client_ip", request.ClientIp)

//        var requestUri = httpRequest.requestURI;
        var logLevelString = httpRequest.queryJson.get("log-level").AsString();

        var logLevel: Level? = null


        if (logLevelString.HasValue) {
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

        if (logLevel != null) {
            usingScope(LogScope.valueOf(logLevel.levelStr.toLowerCase())) {
                next(httpRequest, httpResponse, chain);
            }
        } else {
            next(httpRequest, httpResponse, chain);
        }
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

        var request = MyHttpRequestWrapper.create(_request);
        var response = MyHttpResponseWrapper.create(_response)

        var queryMap = request.queryJson

        if (queryMap.containsKey("callback")) {
            response.characterEncoding = "utf-8";

            setLang(request);

            RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request, response))
            beforeRequest(request);
            try {
                chain?.doFilter(request, response);
            } catch (e: Exception) {
                logger.Error {
                    var msgs = mutableListOf<String>()
                    msgs.add("[[----> ${request.tokenValue} ${request.ClientIp} ${request.method} ${request.fullUrl}")
                    msgs.add(e.message ?: "服务器错误");
                    msgs.add("<----]]")

                    return@Error msgs.joinToString(const.line_break)
                }

                if (request.findParameterStringValue("iniframe").AsBoolean()) {
                    response.parentAlert(e.message ?: "服务器错误")
                } else {
                    response.WriteTextValue(e.message ?: "服务器错误")
                }
                return;
            }
            afterComplete(request, response, queryMap.getStringValue("callback").AsString(), startAt, "");
        } else {

            try {
                chain?.doFilter(request, response)
            } catch (e: Exception) {
                logger.Error {
                    var msgs = mutableListOf<String>()
                    msgs.add("[[----> ${request.tokenValue} ${request.ClientIp} ${request.method} ${request.fullUrl}")
                    msgs.add(e.message ?: "服务器错误");
                    msgs.add("<----]]")
                    return@Error msgs.joinToString(const.line_break)
                }
                if (request.findParameterStringValue("iniframe").AsBoolean()) {
                    response.parentAlert(e.message ?: "服务器错误")
                } else {
                    response.WriteTextValue(e.message ?: "服务器错误")
                }
                return;
            }
//                logNewSession(request, response);
        }


        var endAt = LocalDateTime.now()

        logger.Info {
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

//                var cookie = response.getHeader("Set-Cookie")
//                if (cookie.HasValue) {
//                    msgs.add("Set-Cookie:" + cookie)
//                }
//
//                var contentType = response.getHeader("Content-Type")
//                if (contentType.HasValue) {
//                    msgs.add("Content-Type:" + contentType)
//                }

            msgs.add("<----]]")
            return@Info msgs.joinToString(const.line_break)
        }

    }

//    private fun logNewSession(request: HttpServletRequest, httpResponse: HttpServletResponse) {
//        if (request.session.isNew) {
//            var setCookie = httpResponse.getHeader("Set-Cookie");
//            if (setCookie != null) {
//                logger.info("Set-Cookie: ${setCookie}")
//            }
//        }
//    }

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

            //会不会有 之前 response.write 的情况导致回发数据混乱？
//            response.outputStream.write("""{"msg":${errorMsg}}""".toByteArray(utf8))
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
//                setResponseBid(resValue, request, response);

                if (response.status == 280) {
                    response.result = byteArrayOf();
                } else {
                    response.result = resValue
                }
            }
        }

        var endAt = LocalDateTime.now();
        logger.Info {
            var msg = mutableListOf<String>()
            msg.add("[response] ${request.requestURI} ${response.status} ${endAt - startAt}")

            for (h in response.headerNames) {
                msg.add("\t${h}:${response.getHeader(h)}")
            }

            if (resStringValue.HasValue) {
                msg.add("[response body]:")
                msg.add("\t" + resStringValue.Slice(0, 8192))
            }

            msg.add("<----]]")
            return@Info msg.joinToString(const.line_break)
        }
    }

//    private fun setResponseBid(
//        resValue: ByteArray,
//        request: MyHttpRequestWrapper,
//        response: MyHttpResponseWrapper
//    ): Boolean {
//        if (resValue.isEmpty()) return false;
//        var ori_md5 = request.getHeader("_bid_");
//        if (ori_md5 == null) return false;
//        if (response.status >= 400 || resValue.size < 32) return false;
//
//        var md5 = Md5Util.getBase64Md5(resValue);
//        //body id
//        response.addHeader("_bid_", md5);
//        if (ori_md5.HasValue && ori_md5 == md5) {
//            response.status = 280
//            return true;
//        }
//        return false;
//    }


//    private fun getAllFiles(file: File, filter: ((String) -> Boolean)): List<String> {
//        if (file.isDirectory == false) {
//            if (filter.invoke(file.FullName) == false) {
//                return listOf()
//            }
//
//            return listOf(file.FullName)
//        }
//
//        return file.listFiles().map { getAllFiles(it, filter).toTypedArray() }.Unwind().toList()
//    }
}
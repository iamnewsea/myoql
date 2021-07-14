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
@Order(Ordered.HIGHEST_PRECEDENCE)
//@Configuration
@WebFilter(urlPatterns = ["/*", "/**"])
//@WebFilter(urlPatterns = arrayOf("/**"), filterName = "MyAllFilter")
//@ConfigurationProperties(prefix = "nbcp.filter")
open class MyAllFilter : Filter {
    override fun destroy() {
        MDC.remove("request_id")
    }

    override fun init(p0: FilterConfig?) {
    }

    @Value("\${app.filter.allow-origins:}")
    var allowOrigins: String = "";

    @Value("\${app.filter.headers:/health}")
    var headers: List<String> = listOf()

    /**
     * 静态文件在 resources 里的文件夹。前后不能有 "/"
     */
    @Value("\${app.filter.html-path:public}")
    var htmlPath: String = "public"

    @Value("\${app.filter.ignore-log-urls:}")
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

        if (!(request is HttpServletRequest) ||
//                FileExtentionInfo(request.requestURI).isStaticURI ||
            request.method == "HEAD" ||
            (request is MyHttpRequestWrapper)
        ) {
            chain?.doFilter(request, response)
            return;
        }

        procCORS(httpRequest, httpResponse)

        if (httpRequest.method == "OPTIONS") {
            httpResponse.status = 204
            return;
        }

        var request_id = httpRequest.tokenValue;

        MDC.put("request_id", request_id)
//        MDC.put("user_name", request.LoginUser.name.AsString())
//        MDC.put("client_ip", request.ClientIp)

//        var requestUri = httpRequest.requestURI;
        var logLevelString = httpRequest.queryJson.get("log-level").AsString();

        var logLevel: ch.qos.logback.classic.Level? = null


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
                if (it.endsWith("*")) {
                    return@any httpRequest.requestURI.startsWith(it.Slice(0, -1), true)
                } else {
                    return@any httpRequest.requestURI.equals(it, true)
                }
            }

            if (ignoreLog) {
                logLevel = ch.qos.logback.classic.Level.OFF;
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

//        var ctx = WebApplicationContextUtils.getWebApplicationContext(httpRequest.servletContext)
//        httpRequest.requestCache = ctx.getBean("request", IDataCache4Sql::class.java);
//        httpRequest.requestCache?.clear();

        var startAt = LocalDateTime.now()

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

                RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request, response))
                beforeRequest(myRequest);
                try {
                    chain?.doFilter(myRequest, myResponse);
                } catch (e: Exception) {
                    logger.Error {
                        var msgs = mutableListOf<String>()
                        msgs.add("[[----> ${request.tokenValue} ${request.ClientIp} ${request.method} ${request.fullUrl}")
                        msgs.add(e.message ?: "服务器错误");
                        msgs.add("<----]]")

                        return@Error msgs.joinToString(line_break)
                    }

                    if (request.findParameterStringValue("iniframe").AsBoolean()) {
                        response.parentAlert(e.message ?: "服务器错误")
                    } else {
                        response.WriteTextValue(e.message ?: "服务器错误")
                    }
                    return;
                }
                afterComplete(myRequest, myResponse, queryMap.getStringValue("callback").AsString(), startAt, "");
            } else {

//                //如果是静态资源
//                var file = htmlFiles.firstOrNull { request.requestURI.startsWith(it, true) }
//                if (file == null) {
//                    var browsePath = request.requestURI;
//                    if (browsePath.endsWith("/")) {
//                        browsePath = browsePath.substring(0, browsePath.length - 1);
//                    }
//                    var path = htmlPaths.keys.firstOrNull { it VbSame browsePath };
//                    if (path != null) {
//                        file = path + "/" + htmlPaths.get(path);
//                    }
//                }
//
//                if (file != null) {
//                    file = htmlPath + file;
//                    response.status = 200;
//
//                    var extention = FileExtentionInfo(file)
//                    var contentType = MyUtil.getMimeType(extention.extName)
//                    if (contentType.HasValue) {
//                        response.contentType = contentType
//                    }
//
//                    var resourceResolver = PathMatchingResourcePatternResolver()
//                    var resource = resourceResolver.getResource(file)
//                    resource.inputStream.copyTo(response.outputStream)
//
//                    logger.Info {
//                        var msgs = mutableListOf<String>()
//                        msgs.add("[[----> ${request.tokenValue} ${request.ClientIp} ${request.method} ${request.fullUrl} <----]]")
//                        return@Info msgs.joinToString(line_break)
//                    }
//                    return;
//                }


                try {
                    chain?.doFilter(request, response)
                } catch (e: Exception) {
                    logger.Error {
                        var msgs = mutableListOf<String>()
                        msgs.add("[[----> ${request.tokenValue} ${request.ClientIp} ${request.method} ${request.fullUrl}")
                        msgs.add(e.message ?: "服务器错误");
                        msgs.add("<----]]")
                        return@Error msgs.joinToString(line_break)
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
                    msgs.add("\t${h}:${response.getHeader(h)}")
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
                return@Info msgs.joinToString(line_break)
            }


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

//    private fun logNewSession(request: HttpServletRequest, httpResponse: HttpServletResponse) {
//        if (request.session.isNew) {
//            var setCookie = httpResponse.getHeader("Set-Cookie");
//            if (setCookie != null) {
//                logger.info("Set-Cookie: ${setCookie}")
//            }
//        }
//    }

    private fun procFilter(
        request: MyHttpRequestWrapper,
        response: MyHttpResponseWrapper,
        chain: FilterChain?,
        startAt: LocalDateTime
    ) {
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

                return@Error errorInfo.joinToString(line_break)
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


            var htmlString = (request.body ?: byteArrayOf()).toString(utf8)
            if (htmlString.HasValue) {
                msgs.add("[request body]:")
                msgs.add("\t" + htmlString)
            }


            return@Info msgs.joinToString(line_break)
        }
    }

    private fun procCORS(request: HttpServletRequest, response: HttpServletResponse) {
        var originClient = request.getHeader("origin") ?: ""

        if (originClient.isEmpty()) return

        var allowOrigins = this.allowOrigins.split(",") //非 localhost 域名

        var allow = allowOrigins.any { originClient.contains(it) } ||
                originClient.contains("localhost") ||
                originClient.contains("127.0.0.1");

        if (allow == false) {
            logger.warn("跨域阻止:${originClient}")
            return;
        }

        response.setHeader("Access-Control-Allow-Origin", originClient)
        response.setHeader("Access-Control-Max-Age", "2592000") //30天。

        response.setHeader("Access-Control-Allow-Credentials", "true")
        response.setHeader("Access-Control-Allow-Methods", "GET,POST,PATCH,PUT,HEAD,OPTIONS,DELETE")


        var allowHeaders = mutableSetOf<String>();
        allowHeaders.add(config.tokenKey);

        //添加指定的
        allowHeaders.addAll(headers)

        if (request.method == "OPTIONS") {
            allowHeaders.addAll(
                request.getHeader("Access-Control-Request-Headers").AsString().split(",").filter { it.HasValue })
        }

        if (allowHeaders.any() == false) {
            allowHeaders.addAll(request.headerNames.toList())


            //https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Headers/Access-Control-Expose-Headers
            var standardHeaders = arrayOf(
                "referer",
                "expires",
                "cache-control",
                "content-language",
                "last-modified",
                "pragma",
                "origin",
                "accept",
                "user-agent",
                "connection",
                "host",
                "accept-language",
                "accept-encoding",
                "content-length",
                "content-type"
            )
            //移除标准 header
            allowHeaders.removeAll { standardHeaders.contains(it.toLowerCase()) }
        }


        if (allowHeaders.any()) {
            response.setHeader("Access-Control-Allow-Headers", allowHeaders.joinToString(","))
            response.setHeader("Access-Control-Expose-Headers", allowHeaders.joinToString(","))
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
            response.result = resStringValue.toByteArray(utf8)
        } else if (response.IsOctetContent == false) {
            var resValue = response.result ?: byteArrayOf();
            resStringValue = resValue.toString(utf8);

            if (callback.isNotEmpty() && response.contentType.contains("json")) {
                response.contentType = "application/javascript;charset=UTF-8"
                response.result = """${callback}(${resStringValue})""".toByteArray(utf8)
            } else {
                setResponseBid(resValue, request, response);

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
            return@Info msg.joinToString(line_break)
        }
    }

    private fun setResponseBid(
        resValue: ByteArray,
        request: MyHttpRequestWrapper,
        response: MyHttpResponseWrapper
    ): Boolean {
        if (resValue.isEmpty()) return false;
        var ori_md5 = request.getHeader("_bid_");
        if (ori_md5 == null) return false;
        if (response.status >= 400 || resValue.size < 32) return false;

        var md5 = Md5Util.getBase64Md5(resValue);
        //body id
        response.addHeader("_bid_", md5);
        if (ori_md5.HasValue && ori_md5 == md5) {
            response.status = 280
            return true;
        }
        return false;
    }


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
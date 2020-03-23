package nbcp.web.config

import nbcp.base.extend.*


import nbcp.base.utils.Md5Util
import nbcp.base.utils.MyUtil
import nbcp.comm.JsonMap
import nbcp.comm.*
import nbcp.web.*
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
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
 * 1. server.filter.allowOrigins
 * 2. server.filter.ignore-log-urls
 * 3. server.filter.headers
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
//@Configuration
@WebFilter(urlPatterns = arrayOf("/*", "/**"))
//@WebFilter(urlPatterns = arrayOf("/**"), filterName = "MyAllFilter")
//@ConfigurationProperties(prefix = "nbcp.filter")
open class MyAllFilter : Filter, InitializingBean {
    override fun destroy() {
        MDC.remove("request_id")
    }

    override fun init(p0: FilterConfig?) {
    }

    @Value("\${server.filter.allowOrigins:}")
    var allowOrigins: String = "";
    @Value("\${server.filter.ignore-log-urls:}")
    var ignoreLogUrls: List<String> = listOf()

    @Value("\${server.filter.headers:}")
    var headers: List<String> = listOf()

    @Value("\${server.filter.headers:public}")
    var htmlPath: String = "public"

//    @Value("\${server.session.cookie.name}")
//    var cookieName = "";

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)

        var htmlFiles = listOf<String>()
        var isJarFile = true;
        var jarFile = ""
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

        MDC.put("request_id", httpRequest.session.id.AsString())
//        MDC.put("user_name", request.LoginUser.name.AsString())
//        MDC.put("client_ip", request.ClientIp)

        var ignoreLog = ignoreLogUrls.any {
            httpRequest.requestURI.startsWith(it, true) &&
                    !(it.last().isLetterOrDigit() && (httpRequest.requestURI.getOrNull(it.length)?.isLetterOrDigit()
                            ?: false))
        }

        if (ignoreLog) {
            using(LogScope.FilterNoLog) {
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

        var loginName = request.LoginUser.name.AsString();
        var queryMap = request.queryJson

        if (request.method == "GET") {
            //JSONP
            if (queryMap.containsKey("callback")) {
                var myRequest = MyHttpRequestWrapper(request);
                var myResponse = MyHttpResponseWrapper(response)
                myResponse.characterEncoding = "utf-8";

                setLang(myRequest);

                chain?.doFilter(myRequest, myResponse);

                afterComplete(myRequest, myResponse, queryMap.getStringValue("callback"), startAt, "");
            } else {
                //是否是静态资源, 必须有后缀名。
                var extention = FileExtentionInfo(request.requestURI)
                if (extention.extName.HasValue) {
                    var file = htmlFiles.firstOrNull { request.requestURI.startsWith(it) }
                    if (file != null) {
                        response.status = 200;

                        var contentType = MyUtil.mimeLists.filter { it.key == extention.extName.toLowerCase() }.values.firstOrNull()
                        if (contentType != null) {
                            response.contentType = contentType
                        }

                        var prefix = if (isJarFile) "/" else "";

                        Thread.currentThread().contextClassLoader.getResourceAsStream("${prefix}${htmlPath}${file}").copyTo(response.outputStream)
                        return;
                    }
                }

                chain?.doFilter(request, response)

//                logNewSession(request, response);
            }

            var endAt = LocalDateTime.now()

            logger.Info {
                var msgs = mutableListOf<String>()
                msgs.add("[[----> ${loginName} ${request.ClientIp} ${request.method} ${request.fullUrl}")
                msgs.add("[response] ${response.status} ${endAt - startAt}")

                var cookie = response.getHeader("Set-Cookie")
                if (cookie.HasValue) {
                    msgs.add("Set-Cookie:" + cookie)
                }

                var contentType = response.getHeader("Content-Type")
                if (contentType.HasValue) {
                    msgs.add("Content-Type:" + contentType)
                }

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

        procFilter(myRequest, myResponse, chain, startAt, loginName)
    }

//    private fun logNewSession(request: HttpServletRequest, httpResponse: HttpServletResponse) {
//        if (request.session.isNew) {
//            var setCookie = httpResponse.getHeader("Set-Cookie");
//            if (setCookie != null) {
//                logger.info("Set-Cookie: ${setCookie}")
//            }
//        }
//    }

    private fun procFilter(request: MyHttpRequestWrapper, response: MyHttpResponseWrapper, chain: FilterChain?, startAt: LocalDateTime, loginName: String) {
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request, response))

        beforeRequest(request, loginName)

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
                errorInfo.addAll(err.stackTrace.map { "\t" + it.className + "." + it.methodName + ": " + it.lineNumber }.take(24))

                return@Error errorInfo.joinToString(line_break)
            }

            errorMsg = JsonMap("msg" to errorMsg).ToJson()
            response.status = 500;

            //会不会有 之前 response.write 的情况导致回发数据混乱？
//            response.outputStream.write("""{"msg":${errorMsg}}""".toByteArray(utf8))
        }

        procCORS(request, response);
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

    private fun beforeRequest(request: MyHttpRequestWrapper, loginName: String) {
        logger.Info {
            var msgs = mutableListOf<String>()
            msgs.add("[[----> ${loginName} ${request.ClientIp} ${request.method} ${request.fullUrl}")

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

        if (allow) {
            response.setHeader("Access-Control-Allow-Origin", originClient)
            response.setHeader("Access-Control-Max-Age", "2592000") //30天。

            response.setHeader("Access-Control-Allow-Credentials", "true")
            response.setHeader("Access-Control-Allow-Methods", "GET,POST,PATCH,PUT,HEAD,OPTIONS,DELETE")


            var allowHeaders = mutableSetOf<String>();

            //添加指定的
            allowHeaders.addAll(headers)

            if (request.method == "OPTIONS") {
                allowHeaders.addAll(request.getHeader("Access-Control-Request-Headers").AsString().split(",").filter { it.HasValue })
            }

            if (allowHeaders.any() == false) {
                allowHeaders.addAll(request.headerNames.toList())


                //https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Headers/Access-Control-Expose-Headers
                var standardHeaders = arrayOf(
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

    fun afterComplete(request: MyHttpRequestWrapper, response: MyHttpResponseWrapper, callback: String, startAt: LocalDateTime, errorMsg: String) {
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

    private fun setResponseBid(resValue: ByteArray, request: MyHttpRequestWrapper, response: MyHttpResponseWrapper): Boolean {
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


    private fun getAllFiles(file: File, filter: ((String) -> Boolean)): List<String> {
        if (file.isDirectory) {
            var ret = mutableListOf<String>()
            file.listFiles().forEach {
                ret.addAll(getAllFiles(it, filter));
            }
            return ret
        } else {
            if (filter.invoke(file.FullName) == false) {
                return listOf()
            }

            return listOf(file.FullName)
        }
    }

    //收集静态资源
    override fun afterPropertiesSet() {
        var file = MyUtil.getStartingJarFile();
        if (file.exists() == false) {
            return;
        }
        jarFile = file.FullName;

        if (file.isFile) {
            isJarFile = true;
            var prefix = "BOOT-INF/classes/${htmlPath}/"
            htmlFiles = JarFile(file).entries().toList()
                    .filter { it.name.startsWith(prefix) }
                    .filter { it.name.endsWith("/") == false }
                    .map { "/" + it.name.Slice(prefix.length) }
        } else {
            isJarFile = false;
            var file2 = File(file.FullName + File.separator + htmlPath);
            htmlFiles = getAllFiles(file2) { it.startsWith(file2.FullName + File.separator) && (it.endsWith(File.separator) == false) }
                    .map { "/" + it.Slice(file2.FullName.length + 1) }
        }
    }
}
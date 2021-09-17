package nbcp.filter

import nbcp.comm.*
import nbcp.utils.*
import nbcp.web.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
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
open class CrosFilter : Filter {
    @Value("\${app.filter.allow-origins:}")
    var allowOrigins: String = "";

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        if (request is HttpServletRequest == false) {
            chain.doFilter(request, response)
            return;
        }

        var httpRequest = request as HttpServletRequest
        var httpResponse = response as HttpServletResponse

        HttpContext.init(httpRequest, httpResponse);

        var request2: MyHttpRequestWrapper? = null
        httpRequest.getCorsResponseMap(this.allowOrigins.split(",")).apply {
            if (this.any() && httpRequest.method != "OPTIONS") {
                request2 = MyHttpRequestWrapper.create(httpRequest);
                request2!!.removeHeader("origin")

                var originClient = request.getHeader("origin") ?: ""
                logger.Important("处理了跨域,并移除了origin, 请求源:${originClient}")
            }
        }.forEach { key, value ->
            httpResponse.setHeader(key, value);
        }

        if (httpRequest.method == "OPTIONS") {
            httpResponse.status = 204
            return;
        }

        chain.doFilter(request2 ?: request, response)
    }
}
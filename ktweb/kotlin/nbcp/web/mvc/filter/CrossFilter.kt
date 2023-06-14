package nbcp.web.mvc.filter

import nbcp.base.comm.*
import nbcp.base.db.*
import nbcp.base.enums.*
import nbcp.base.extend.*
import nbcp.base.utils.*
import nbcp.mvc.mvc.MyHttpRequestWrapper
import nbcp.mvc.mvc.HttpContext
import nbcp.mvc.mvc.fullUrl
import nbcp.mvc.mvc.getCorsResponseMap
import nbcp.myoql.db.db
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
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
@ConditionalOnClass(Filter::class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
open class MyOqlCrossFilter : Filter {
    @Value("\${app.filter.allow-origins:*}")
    var ALLOW_ORIGINS: String = "*";

    /**
     * 可以定义禁止的 header,默认允许通过所有 Header
     */
    @Value("\${app.filter.deny-headers:}")
    var DENY_HEADERS: List<String> = listOf()

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        if (request is HttpServletRequest == false) {
            chain.doFilter(request, response)
            return;
        }

        val httpRequest = request
        if (httpRequest.getAttribute("(CrossFilterProced)").AsBoolean()) {
            chain.doFilter(request, response)
            return
        }
        httpRequest.setAttribute("(CrossFilterProced)", true);


        var httpResponse = response as HttpServletResponse

        var request2: HttpServletRequest? = null
        httpRequest.getCorsResponseMap(this.ALLOW_ORIGINS.split(","), DENY_HEADERS).apply {
            if (this.any() && httpRequest.method != "OPTIONS") {
                request2 = MyAllFilter.getRequestWrapper(httpRequest);

                (request2 as MyHttpRequestWrapper).removeHeader("origin")

                val originClient = request.getHeader("origin") ?: ""
                logger.Important("处理了跨域,并移除了origin, 请求源:${originClient}, url:${httpRequest.fullUrl}")
            }
        }.forEach { key, value ->
            httpResponse.setHeader(key, value);
        }

        if (httpRequest.method == "OPTIONS") {
            httpResponse.status = 204
            return;
        }

        val targetRequest = request2 ?: request;


        HttpContext.init(targetRequest, httpResponse);
        db.currentRequestChangeDbTable.clear()

        chain.doFilter(targetRequest, response)
    }
}
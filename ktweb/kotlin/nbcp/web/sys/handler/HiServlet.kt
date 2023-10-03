package nbcp.web.sys.handler

import nbcp.base.comm.config
import nbcp.base.extend.*
import nbcp.mvc.annotation.*
import nbcp.mvc.sys.*
import nbcp.myoql.db.cache.RedisCacheAopService
import nbcp.web.comm.WebAppInfo
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


/**
 * Created by udi on 17-4-6.
 * 1. HandlerInterceptorAdapter 不会拦截 HttpServlet。
 * 2. 不使用 @Controller 注解，不能生成Bean，不能使用 Aop
 */
@OpenAction
@RestController
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
open class HiServlet {
    @GetMapping("/hi")
    fun doGet(request: HttpServletRequest, response: HttpServletResponse) {

        val sleep = (request.findParameterValue("sleep").AsFloat() * 1000).toLong();
        if (sleep > 0 && sleep <= 3600_000) {
            Thread.sleep(sleep);
        }

        val status = request.findParameterValue("status").AsInt()
        if (status.HasValue) {
            response.status = status
        }


        var envKeys = request.getParameter("env-key").AsString().split(",").filter { it.HasValue }
        if (envKeys.any()) {
//            val env = SpringUtil.context.environment;
            var value = "";
            for (envKey in envKeys) {
                value = config.getConfig(envKey).AsString();
                if (value.HasValue) {
                    response.WriteTextValue(value)
                    return;
                }
            }

            return;
        }

        var jobMonitor = request.getParameter("job-monitor").AsBooleanWithNull()
        if (jobMonitor ?: false) {
            response.WriteJsonRawValue(RedisCacheAopService.jobMonitor.toJsonString())
            return;
        }


        WebAppInfo.getAppInfo(request.ClientIp).apply {
            if (this.HasValue) {
                response.WriteHtmlBodyValue(this);
            }
        }
    }
}


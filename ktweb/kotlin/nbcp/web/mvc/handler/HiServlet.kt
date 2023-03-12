package nbcp.web.mvc.handler

import nbcp.base.extend.AsFloat
import nbcp.base.extend.AsInt
import nbcp.base.extend.AsString
import nbcp.base.extend.HasValue
import nbcp.base.utils.ClassUtil
import nbcp.base.utils.JarUtil
import nbcp.base.utils.SpringUtil
import nbcp.mvc.mvc.WriteHtmlBodyValue
import nbcp.mvc.mvc.WriteHtmlValue
import nbcp.mvc.mvc.findParameterValue
import nbcp.mvc.annotation.*
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
            return;
        }


        var key = request.getParameter("key")
        if (key.HasValue) {
            val env = SpringUtil.context.environment;
            var value = env.getProperty(key)
            if (value != null) {
                response.WriteHtmlValue(value)
                return;
            } else {
                return;
            }
        }


        WebAppInfo.getAppInfo().apply {
            if (this.HasValue) {
                response.WriteHtmlBodyValue(this);
            }
        }
    }
}


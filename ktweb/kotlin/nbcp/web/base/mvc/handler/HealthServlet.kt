package nbcp.web.base.mvc.handler


import nbcp.base.comm.*
import nbcp.mvc.comm.OpenAction
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

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
open class HealthServlet {

    @GetMapping("/health")
    fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
        response.status = 200;
    }
}


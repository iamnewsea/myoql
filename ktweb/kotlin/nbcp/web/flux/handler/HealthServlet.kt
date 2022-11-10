package nbcp.web.flux.handler

import nbcp.mvc.comm.OpenAction
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange


/**
 * Created by udi on 17-4-6.
 * 1. HandlerInterceptorAdapter 不会拦截 HttpServlet。
 * 2. 不使用 @Controller 注解，不能生成Bean，不能使用 Aop
 */
@OpenAction
@RestController
//@ConditionalOnClass(Publisher::class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
//@ConditionalOnMissingClass("javax.servlet.http.HttpServletRequest")
open class HealthServlet {
    @GetMapping("/health")
    fun doGet(swe: ServerWebExchange) {
    }
}


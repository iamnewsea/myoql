package nbcp.web.flux.handler

import nbcp.base.extend.AsString
import nbcp.base.extend.HasValue
import nbcp.base.extend.getStringValue
import nbcp.base.utils.ClassUtil
import nbcp.base.utils.JarUtil
import nbcp.base.utils.SpringUtil
import nbcp.mvc.annotation.OpenAction
import nbcp.mvc.flux.ClientIp
import nbcp.web.comm.WebAppInfo
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.util.*


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
open class HiServlet {
    @GetMapping("/hi")
    fun doGet(swe: ServerWebExchange): Mono<String> {

        var key = swe.request.queryParams.getStringValue("key").AsString()
        if (key.HasValue) {
            val env = SpringUtil.context.environment;
            var value = env.getProperty(key)
            if (value != null) {
                return Mono.just(value)
            } else {
                return Mono.empty()
            }
        }

        WebAppInfo.getAppInfo(swe.ClientIp).apply {
            if (this.HasValue) {
                return Mono.just(this);
            } else {
                return Mono.empty()
            }
        }
    }
}


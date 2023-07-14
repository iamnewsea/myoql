package nbcp.web.mvc.handler

import com.wf.captcha.ArithmeticCaptcha
import nbcp.base.event.SetValidateCodeEvent
import nbcp.base.extend.AsInt
import nbcp.base.utils.SpringUtil
import nbcp.mvc.mvc.queryJson
import nbcp.mvc.annotation.*
import nbcp.web.extend.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


/**
 * Created by udi on 17-4-6.
 * https://gitee.com/whvse/EasyCaptcha
 * 事实上， HandlerInterceptorAdapter 不会拦截 HttpServlet
 */
@OpenAction
@RestController
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
//@ConditionalOnBean(IUserAuthenticationService::class)
open class AuthImageServlet {


    @GetMapping("/open/validate-code-image")
    fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
        var token = request.tokenValue
        if (token.isNullOrEmpty()) {
            throw RuntimeException("找不到token")
        }

        var width = request.queryJson.get("width").AsInt(130)
        var height = request.queryJson.get("height").AsInt(48)

        var captcha = ArithmeticCaptcha(width, height);
        var txt = captcha.text()

        var ev = SetValidateCodeEvent(token);
        ev.result = txt;
        SpringUtil.context.publishEvent(ev);
        response.setHeader("content-type", "image/png")

        captcha.out(response.outputStream);
    }
}


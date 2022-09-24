package nbcp.base.mvc.handler

import com.wf.captcha.ArithmeticCaptcha
import nbcp.base.mvc.service.IUserAuthenticationService
import nbcp.comm.*
import nbcp.utils.SpringUtil
import nbcp.base.mvc.queryJson
import nbcp.web.tokenValue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.lang.RuntimeException
import javax.servlet.Filter
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

    val userSystemService: IUserAuthenticationService by lazy {
        return@lazy SpringUtil.getBean()
    }

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

        userSystemService.setValidateCode(token, txt);
        response.setHeader("content-type", "image/png")

        captcha.out(response.outputStream);
    }
}


package nbcp.base.handler

import ch.qos.logback.classic.Level
import com.wf.captcha.ArithmeticCaptcha
import com.wf.captcha.utils.CaptchaUtil
import nbcp.comm.*
import nbcp.db.db
import nbcp.utils.CodeUtil
import nbcp.web.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.lang.RuntimeException

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


/**
 * Created by udi on 20-8-27.
 */
@RestController
open class TokenGeneratorServlet {
    /**
     * 由于 SameSite 阻止跨域 Set-Cookie 的问题，所以使用请求参数 token 代替 cookie
     */
//    @Value("\${app.token-name:token}")
//    var tokenName: String = ""

    @GetMapping("/open/token")
    fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
        var token = request.tokenValue;
        response.WriteJsonRawValue(ApiResult.of(token).ToJson())
    }
}


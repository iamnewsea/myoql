package nbcp.handler

import ch.qos.logback.classic.Level
import com.wf.captcha.ArithmeticCaptcha
import com.wf.captcha.utils.CaptchaUtil
import nbcp.comm.*
import nbcp.db.db
import nbcp.utils.CodeUtil
import nbcp.web.*
import org.springframework.beans.factory.annotation.Value
import java.lang.RuntimeException
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServlet

/**
 * Created by udi on 20-8-27.
 */
@WebServlet(urlPatterns = ["/open/token"])
open class TokenGeneratorServlet : HttpServlet() {
    /**
     * 由于 SameSite 阻止跨域 Set-Cookie 的问题，所以使用请求参数 token 代替 cookie
     */
//    @Value("\${app.token-name:token}")
//    var tokenName: String = ""

    override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
        var token = request.tokenValue;
        response.WriteJsonRawValue(ApiResult.of(token).ToJson())
    }

    override fun doPost(request: HttpServletRequest, response: HttpServletResponse) {
        var token = request.tokenValue;
        response.WriteJsonRawValue(ApiResult.of(token).ToJson())
    }
}


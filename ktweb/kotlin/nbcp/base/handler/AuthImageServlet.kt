package nbcp.base.handler

import com.wf.captcha.ArithmeticCaptcha
import nbcp.comm.*
import nbcp.base.service.UserAuthenticationService
import nbcp.web.queryJson
import nbcp.web.tokenValue
import org.springframework.beans.factory.annotation.Autowired
import java.lang.RuntimeException
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServlet

/**
 * Created by udi on 17-4-6.
 * https://gitee.com/whvse/EasyCaptcha
 * 事实上， HandlerInterceptorAdapter 不会拦截 HttpServlet
 */
@WebServlet(urlPatterns = ["/open/validate-code-image"])
open class AuthImageServlet : HttpServlet() {
    @Autowired
    lateinit var userSystemService: UserAuthenticationService;

    override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
        var token = request.tokenValue
        if (token.isNullOrEmpty()) {
            throw RuntimeException("找不到token")
        }

        var width = request.queryJson.get("width").AsInt(130)
        var height = request.queryJson.get("height").AsInt(48)

        var captcha = ArithmeticCaptcha(width, height);
        var txt = captcha.text()

        userSystemService.validateCodeRedis.set(token, txt);
        response.setHeader("content-type", "image/png")

//        var set_cookie_ori = response.getHeader("Set-Cookie") ?: "";
//        if (set_cookie_ori.contains(" SameSite=", true) == false) {
//            response.setHeader("Set-Cookie", set_cookie_ori + "; SameSite=Lax");
//        }
        captcha.out(response.outputStream);
    }
}


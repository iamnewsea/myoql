package nbcp.handler

import ch.qos.logback.classic.Level
import com.wf.captcha.ArithmeticCaptcha
import nbcp.comm.*
import nbcp.db.db
import nbcp.web.queryJson
import nbcp.web.tokenValue
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
@OpenAction
@MyLogLevel(LogScope.error)
@WebServlet(urlPatterns = ["/open/validate-code-image"])
open class AuthImageServlet : HttpServlet() {

    override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
        var request_id = request.tokenValue
        if (request_id.isNullOrEmpty()) {
            throw RuntimeException("找不到token")
        }

        var width = request.queryJson.get("width").AsInt(130)
        var height = request.queryJson.get("height").AsInt(48)

        var captcha = ArithmeticCaptcha(width, height);
        var txt = captcha.text()

        db.rer_base.userSystem.validateCode.set(request_id, txt);
        response.setHeader("content-type", "image/png")

//        var set_cookie_ori = response.getHeader("Set-Cookie") ?: "";
//        if (set_cookie_ori.contains(" SameSite=", true) == false) {
//            response.setHeader("Set-Cookie", set_cookie_ori + "; SameSite=Lax");
//        }
        captcha.out(response.outputStream);
    }
}


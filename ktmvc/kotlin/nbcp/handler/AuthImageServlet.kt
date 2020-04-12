package nbcp.handler

import ch.qos.logback.classic.Level
import com.wf.captcha.ArithmeticCaptcha
import com.wf.captcha.utils.CaptchaUtil
import nbcp.comm.*
import nbcp.db.db
import nbcp.web.queryJson
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
@MyLogLevel(Level.ERROR_INT)
@WebServlet(urlPatterns = arrayOf("/open/validate-code-image"))
open class AuthImageServlet : HttpServlet() {
    override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
        var width = request.queryJson.get("width").AsInt(130)
        var height = request.queryJson.get("height").AsInt(48)

        var captcha = ArithmeticCaptcha(width, height);
        var txt = captcha.text()
        db.rer_base.validateCode.set(request.session.id, txt);
        captcha.out(response.outputStream);
    }
}


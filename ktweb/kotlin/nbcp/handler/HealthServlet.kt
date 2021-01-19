package nbcp.handler

import ch.qos.logback.classic.Level
import com.wf.captcha.ArithmeticCaptcha
import com.wf.captcha.utils.CaptchaUtil
import nbcp.comm.*
import nbcp.db.db
import nbcp.web.findParameterStringValue
import nbcp.web.queryJson
import nbcp.web.tokenValue
import org.springframework.beans.factory.annotation.Value
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
@MyLogLevel("off")
@WebServlet(urlPatterns = ["/health"])
open class HealthServlet : HttpServlet() {

    override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
        response.status = 200;
    }

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.status = 200;
    }

    override fun doHead(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.status = 200;
    }

    override fun doOptions(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.status = 200;
    }
}


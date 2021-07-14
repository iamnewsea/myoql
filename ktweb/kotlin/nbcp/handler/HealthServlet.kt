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
 * 1. HandlerInterceptorAdapter 不会拦截 HttpServlet。
 * 2. 不使用 @Controller 注解，不能生成Bean，不能使用 Aop
 */
@WebServlet(urlPatterns = ["/health"])
open class HealthServlet : HttpServlet() {

    public override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
        response.status = 200;
    }

    public override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.status = 200;
    }

    public override fun doHead(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.status = 200;
    }

    public override fun doOptions(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.status = 200;
    }
}


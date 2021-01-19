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
@OpenAction
@MyLogLevel("warn")
@WebServlet(urlPatterns = ["/open/code"])
open class CodeServlet : HttpServlet() {

    override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
        var code = CodeUtil.getCode();
        response.WriteJsonRawValue(ApiResult.of(code).ToJson())
    }

    override fun doPost(request: HttpServletRequest, response: HttpServletResponse) {
        var code = CodeUtil.getCode();
        response.WriteJsonRawValue(ApiResult.of(code).ToJson())
    }
}


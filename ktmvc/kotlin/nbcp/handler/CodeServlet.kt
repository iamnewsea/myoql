package nbcp.handler

import ch.qos.logback.classic.Level
import com.wf.captcha.ArithmeticCaptcha
import com.wf.captcha.utils.CaptchaUtil
import nbcp.comm.*
import nbcp.db.db
import nbcp.utils.CodeUtil
import nbcp.web.findParameterStringValue
import nbcp.web.generateToken
import nbcp.web.queryJson
import nbcp.web.tokenValue
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
@MyLogLevel(Level.ERROR_INT)
@WebServlet(urlPatterns = arrayOf("/open/code"))
open class CodeServlet : HttpServlet() {

    override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
        var code = CodeUtil.getCode();
        response.outputStream.write(ApiResult.of(code).ToJson().toByteArray(utf8))
    }

    override fun doPost(request: HttpServletRequest, response: HttpServletResponse) {
        var code = CodeUtil.getCode();
        response.outputStream.write(ApiResult.of(code).ToJson().toByteArray(utf8))
    }
}


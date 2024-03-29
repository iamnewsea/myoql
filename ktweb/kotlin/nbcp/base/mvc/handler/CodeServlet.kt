package nbcp.base.mvc.handler

import ch.qos.logback.classic.Level
import com.wf.captcha.ArithmeticCaptcha
import com.wf.captcha.utils.CaptchaUtil
import nbcp.comm.*
import nbcp.db.db
import nbcp.utils.CodeUtil
import nbcp.base.mvc.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.lang.RuntimeException

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


/**
 * Created by udi on 20-8-27.
 */
@OpenAction
@RestController
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
open class CodeServlet {

    @GetMapping("/open/code")
    fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
        var code = CodeUtil.getCode();
        response.WriteJsonRawValue(ApiResult.of(code).ToJson())
    }

//    @PostMapping("/open/code")
//    fun doPost(request: HttpServletRequest, response: HttpServletResponse) {
//        var code = CodeUtil.getCode();
//        response.WriteJsonRawValue(ApiResult.of(code).ToJson())
//    }
}


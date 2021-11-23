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


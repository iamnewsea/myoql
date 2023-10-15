package nbcp.web.sys.handler

import nbcp.base.comm.ApiResult
import nbcp.base.extend.*
import nbcp.mvc.sys.WriteJsonRawValue
import nbcp.mvc.annotation.*
import nbcp.web.extend.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


/**
 * Created by udi on 20-8-27.
 */
@OpenAction
@RestController
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
open class TokenGeneratorServlet {
    /**
     * 由于 SameSite 阻止跨域 Set-Cookie 的问题，所以使用请求参数 token 代替 cookie
     */
//    @Value("\${app.token-name:token}")
//    var tokenName: String = ""

    @GetMapping("/open/token")
    fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
        var token = request.tokenValue;
        response.WriteJsonRawValue(ApiResult.of(token).ToJson())
    }
}


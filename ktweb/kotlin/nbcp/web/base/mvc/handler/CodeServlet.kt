package nbcp.web.base.mvc.handler

import nbcp.base.extend.ToJson
import nbcp.base.utils.CodeUtil
import nbcp.mvc.base.mvc.WriteJsonRawValue
import nbcp.mvc.comm.OpenAction
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
open class CodeServlet {

    @GetMapping("/open/code")
    fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
        var code = CodeUtil.getCode();
        response.WriteJsonRawValue(nbcp.base.comm.ApiResult.of(code).ToJson())
    }

//    @PostMapping("/open/code")
//    fun doPost(request: HttpServletRequest, response: HttpServletResponse) {
//        var code = CodeUtil.getCode();
//        response.WriteJsonRawValue(ApiResult.of(code).ToJson())
//    }
}


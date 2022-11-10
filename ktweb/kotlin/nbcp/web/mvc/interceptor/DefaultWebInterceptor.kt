package nbcp.web.mvc.interceptor


import nbcp.base.comm.config
import nbcp.base.extend.AsBoolean
import nbcp.base.extend.IsIn
import nbcp.mvc.mvc.WriteTextValue
import nbcp.mvc.mvc.findParameterStringValue
import nbcp.mvc.mvc.parentAlert
import nbcp.mvc.comm.AdminSysOpsAction
import nbcp.mvc.comm.OpenAction
import nbcp.web.extend.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Created by yuxh on 2019/1/17
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
class DefaultWebInterceptor : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (response.status >= 400 && response.status <= 600) {
            return true;
        }

        if (handler is HandlerMethod == false) {
            return true;
        }

        if (request.requestURI.startsWith("/open/", true) ||
            request.requestURI.startsWith("/open-", true)
        ) {
            return true;
        }

        var beanType = handler.beanType;

        if (beanType.name.IsIn(
                listOf(
                    // Swagger2.9.2
                    "springfox.documentation.swagger.web.ApiResourceController",
                    //Swagger3.0.0
                    "springfox.documentation.swagger2.web.Swagger2ControllerWebMvc",
                    "springfox.documentation.oas.web.OpenApiControllerWebMvc"
                )
            )
        ) {
            return true;
        }
        if (beanType.annotations.any { it is OpenAction }) {
            return true;
        }
        if (beanType.annotations.any { it is AdminSysOpsAction }) {
            if (request.findParameterStringValue("admin-token") == config.adminToken)
                return true;
        }

        if (request.LoginUser.id.isEmpty()) {
            response.status = 401;
            if (request.findParameterStringValue("iniframe").AsBoolean()) {
                response.parentAlert("您需要登录")
            } else {
                response.WriteTextValue("您需要登录");
            }
            return false;
        }

        return super.preHandle(request, response, handler)
    }

    override fun postHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        modelAndView: ModelAndView?
    ) {
        super.postHandle(request, response, handler, modelAndView)
    }
}
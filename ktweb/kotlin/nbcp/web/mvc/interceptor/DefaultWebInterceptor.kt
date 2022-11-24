package nbcp.web.mvc.interceptor


import nbcp.base.comm.config
import nbcp.base.extend.AsBoolean
import nbcp.base.extend.HasValue
import nbcp.base.extend.IsIn
import nbcp.mvc.mvc.WriteTextValue
import nbcp.mvc.mvc.findParameterStringValue
import nbcp.mvc.mvc.parentAlert
import nbcp.mvc.annotation.*
import nbcp.mvc.annotation.*
import nbcp.web.extend.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Configuration
import org.springframework.util.AntPathMatcher
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

    @Value("\${app.web.interceptor.open-bean:}")
    var OPEN_BEAN: String = ""


    @Value("\${app.web.interceptor.open-url:}")
    var OPEN_URL: String = ""

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        //如果出现了错误。 就不允许执行了。
        if (response.status >= 400 && response.status <= 600) {
            return false;
        }

        if (handler is HandlerMethod == false) {
            return true;
        }

        val requestURL = request.requestURI;

        if (requestURL.startsWith("/open/", true) ||
            requestURL.startsWith("/open-", true)
        ) {
            return true;
        }

        var matcher = AntPathMatcher(".")
        if (OPEN_URL.HasValue &&
            OPEN_URL.split(",").any { matcher.match(it, requestURL) }
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
                    "springfox.documentation.oas.web.OpenApiControllerWebMvc",
                    //SpringDoc
                    "org.springdoc.webmvc.ui.SwaggerWelcomeWebMvc",
                    "org.springdoc.webmvc.ui.SwaggerConfigResource",
                    "org.springdoc.webmvc.api.OpenApiWebMvcResource"
                )
            )
        ) {
            return true;
        }

        /**
         * 可以这样定义：  springfox2.*,org.springdoc2.*.ui.*,org.springdc2.*.api.*
         */
        if (OPEN_BEAN.HasValue &&
            OPEN_BEAN.split(",").any { matcher.match(it, beanType.name) }
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
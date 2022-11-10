package nbcp.web.comm

import nbcp.base.db.LoginUserModel
import nbcp.web.LoginUser
import org.slf4j.LoggerFactory
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

/**
 * 在Mvc参数中直接使用 LoginUserModel
 */
class LoginUserParameterConverter() : HandlerMethodArgumentResolver {

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        if (parameter.parameterType == LoginUserModel::class.java) {
            return true;
        }

        return false;
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        nativeRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        if (mavContainer == null || binderFactory == null) return null

        //现在只支持ServletWebRequest
        val webRequest = (nativeRequest as ServletWebRequest).request

        return webRequest.LoginUser;
    }
}



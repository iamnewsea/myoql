package nbcp.web.comm

import nbcp.base.db.LoginUserModel
import nbcp.web.extend.LoginUser
import org.slf4j.LoggerFactory
import org.springframework.core.MethodParameter
import org.springframework.core.Ordered
import org.springframework.http.HttpStatus
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

/**
 * 在Mvc参数中直接使用 LoginUserModel
 */
class LoginUserParameterConverter() : HandlerMethodArgumentResolver, Ordered {

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

        val ret = webRequest.LoginUser;

        if (ret.id.isNullOrEmpty()) {
            mavContainer.status = HttpStatus.UNAUTHORIZED;
            nativeRequest.response.status = 401;
            throw HttpClientErrorException(HttpStatus.UNAUTHORIZED);
        }

        return ret;
    }

    override fun getOrder(): Int {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}



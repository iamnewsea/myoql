package nbcp.web

import org.springframework.context.EnvironmentAware
import org.springframework.core.convert.support.GenericConversionService
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter


import nbcp.utils.*
import java.io.InputStream
import java.nio.charset.Charset
import java.time.LocalDate
import java.util.*
import javax.servlet.ServletContext
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession
import kotlin.concurrent.getOrSet

/**
 * 当前连接的上下文信息
 */
object HttpContext {
    private var _request: ThreadLocal<HttpServletRequest?> = ThreadLocal.withInitial { null }
    private var _response: ThreadLocal<HttpServletResponse?> = ThreadLocal.withInitial { null }

    fun init(request: HttpServletRequest, response: HttpServletResponse) {
        this._request.set(request);
        this._response.set(response);
    }

    @JvmStatic
    val hasRquest: Boolean
        get() {
            if (_request.get() != null) return true;
            if ((RequestContextHolder.getRequestAttributes() as ServletRequestAttributes?)?.request != null) {
                return true;
            }
            return false;
        }

    @JvmStatic
    val hasResponse: Boolean
        get() {
            if (_response.get() != null) return true;
            if ((RequestContextHolder.getRequestAttributes() as ServletRequestAttributes?)?.response != null) {
                return true;
            }
            return false;
        }

    @JvmStatic
    val request: HttpServletRequest
        get() {
            return _request.get()
                ?: (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes?)?.request!!
        }

    @JvmStatic
    val response: HttpServletResponse
        get() {
            return _response.get()
                ?: (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes?)?.response!!
        }

    //(RequestContextHolder.getRequestAttributes() as ServletRequestAttributes?)?.response!!

//    @JvmStatic
//    val userName: String = SystemContext.userName
}

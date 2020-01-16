package nbcp.web

import org.springframework.context.EnvironmentAware
import org.springframework.core.convert.support.GenericConversionService
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter


import nbcp.base.utils.MyUtil
import nbcp.base.utils.SpringUtil
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
 * Created by udi on 17-5-22.
 */
object HttpContext {
    @JvmStatic
    val request: HttpServletRequest
        get() {
            return (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes?)?.request!!
        }

//    @JvmStatic
//    val session: HttpSession
//        get() {
//            return request.session;
//        }

    @JvmStatic
    val response: HttpServletResponse
        get() {
            return (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes?)?.response!!
        }

    @JvmStatic
    val servletContext: ServletContext
        get() {
            return request.servletContext!!
        }

    @JvmStatic
    val nullableRequest: HttpServletRequest?
        get() {
            return (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes?)?.request
        }




//    @JvmStatic
//    val userName: String = SystemContext.userName
}

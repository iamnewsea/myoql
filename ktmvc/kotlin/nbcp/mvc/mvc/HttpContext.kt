@file:JvmName("MyMvcHelper")
@file:JvmMultifileClass

package nbcp.mvc.mvc


import nbcp.mvc.comm.HttpFeignLogData
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * 当前连接的上下文信息
 */
object HttpContext {
    private var _request = ThreadLocal.withInitial<HttpServletRequest?> { null }
    private var _response = ThreadLocal.withInitial<HttpServletResponse?> { null }
    private var _last_feign = ThreadLocal.withInitial<HttpFeignLogData?> { null }

    @JvmStatic
    fun init(request: HttpServletRequest, response: HttpServletResponse) {
        _request.set(request);
        _response.set(response);
    }

    @JvmStatic
    val hasRequest: Boolean
        get() {
            if (_request.get() != null) return true;
            if ((RequestContextHolder.getRequestAttributes() as ServletRequestAttributes?)?.request != null) {
                return true;
            }
            return false;
        }

    var lastFeign: HttpFeignLogData?
        get() {
            return _last_feign.get()
        }
        set(value) {
            _last_feign.set(value);
        }

//    @JvmStatic
//    val hasResponse: Boolean
//        get() {
//            if (_response.get() != null) return true;
//            if ((RequestContextHolder.getRequestAttributes() as ServletRequestAttributes?)?.response != null) {
//                return true;
//            }
//            return false;
//        }

    @JvmStatic
    val request: HttpServletRequest
        get() {
            return (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes?)?.request
                    ?: _request.get()
                    ?: throw RuntimeException("找不到 HttpServletRequest")
        }

    @JvmStatic
    val response: HttpServletResponse
        get() {
            return (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes?)?.response
                    ?: _response.get()
                    ?: throw RuntimeException("找不到 HttpServletResponse")
        }

    //(RequestContextHolder.getRequestAttributes() as ServletRequestAttributes?)?.response!!

//    @JvmStatic
//    val userName: String = SystemContext.userName
}

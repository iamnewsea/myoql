package nbcp.base.flux

import java.lang.RuntimeException
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse

/**
 * 当前连接的上下文信息
 */
object HttpContext {
    private var _request: ThreadLocal<ServerHttpRequest?> = ThreadLocal.withInitial { null }
    private var _response: ThreadLocal<ServerHttpResponse?> = ThreadLocal.withInitial { null }

    fun init(request: ServerHttpRequest, response: ServerHttpResponse) {
        _request.set(request);
        _response.set(response);
    }

//    @JvmStatic
//    val hasRequest: Boolean
//        get() {
//            if (_request.get() != null) return true;
//            if ((RequestContextHolder.getRequestAttributes() as ServletRequestAttributes?)?.request != null) {
//                return true;
//            }
//            return false;
//        }
//
//    @JvmStatic
//    val hasResponse: Boolean
//        get() {
//            if (_response.get() != null) return true;
//            if ((RequestContextHolder.getRequestAttributes() as ServletRequestAttributes?)?.response != null) {
//                return true;
//            }
//            return false;
//        }
//
//    @JvmStatic
//    val request: ServerHttpRequest
//        get() {
//            return (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes?)?.request
//                ?: _request.get()
//                ?: throw RuntimeException("找不到 ServerHttpRequest")
//        }
//
//    @JvmStatic
//    val response: ServerHttpResponse
//        get() {
//            return (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes?)?.response
//                ?: _response.get()
//                ?: throw RuntimeException("找不到 ServerHttpResponse")
//        }

    //(RequestContextHolder.getRequestAttributes() as ServletRequestAttributes?)?.response!!

//    @JvmStatic
//    val userName: String = SystemContext.userName
}

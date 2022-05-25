package nbcp.base.flux

import java.lang.RuntimeException
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.server.ServerWebExchange

/**
 * 当前连接的上下文信息
 */
object HttpContext {
    private var _exchange = ThreadLocal.withInitial<ServerWebExchange?> { null }

    fun init(exchange: ServerWebExchange) {
        _exchange.set(exchange);
    }

    @JvmStatic
    val hasRequest: Boolean
        get() {
            if (_exchange.get() != null) return true;

            return false;
        }

    @JvmStatic
    val request: ServerHttpRequest
        get() {
            return _exchange.get()?.request
                ?: throw RuntimeException("找不到 ServerWebExchange.ServerHttpRequest")
        }

    @JvmStatic
    val response: ServerHttpResponse
        get() {
            return _exchange.get()?.response
                ?: throw RuntimeException("找不到 ServerWebExchange.ServerHttpResponse")
        }

    //(RequestContextHolder.getRequestAttributes() as ServletRequestAttributes?)?.response!!

//    @JvmStatic
//    val userName: String = SystemContext.userName
}

package nbcp.base


import org.slf4j.LoggerFactory
import java.io.*
import java.util.*
import javax.servlet.http.*
import org.springframework.web.util.ContentCachingRequestWrapper

/**
 * Created by udi on 17-4-3.
 */

/**
 * 配置 server.servlet.max-http-post-size 设置请求体的大小，默认 2MB
 */
class MyHttpRequestWrapper
@Throws(IOException::class)
private constructor(request: HttpServletRequest) : ContentCachingRequestWrapper(request) {

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java);

        @Throws(IOException::class)
        @JvmStatic
        fun create(request: HttpServletRequest): MyHttpRequestWrapper {
            if (request is MyHttpRequestWrapper) {
                return request
            }
            return MyHttpRequestWrapper(request)
        }
    }

    private val excludeHeaderNames = mutableSetOf<String>()
    fun removeHeader(headerName: String) {
        excludeHeaderNames.add(headerName)
    }

    override fun getHeaderNames(): Enumeration<String> {
        if (excludeHeaderNames.any() == false) {
            return super.getHeaderNames();
        }

        return Vector<String>(super.getHeaderNames().toList() - excludeHeaderNames).elements()
    }
}


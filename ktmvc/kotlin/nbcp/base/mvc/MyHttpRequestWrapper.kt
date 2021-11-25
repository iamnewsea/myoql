package nbcp.base.mvc


import nbcp.comm.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import nbcp.comm.*

import java.nio.charset.Charset
import javax.servlet.ReadListener
import javax.servlet.ServletInputStream
import nbcp.utils.*
import java.io.*
import java.util.*
import javax.servlet.ServletContext
import javax.servlet.http.*
import nbcp.db.IdName
import nbcp.web.IsOctetContent
import nbcp.web.queryJson
import org.springframework.util.unit.DataSize
import java.lang.RuntimeException

/**
 * Created by udi on 17-4-3.
 */

/**
 * 配置 server.servlet.max-http-post-size 设置请求体的大小，默认 2MB
 */
class MyHttpRequestWrapper
@Throws(IOException::class)
private constructor(request: HttpServletRequest) : HttpServletRequestWrapper(request) {

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

    //文件上传或 大于 10 MB 会返回 null , throw RuntimeException("超过10MB不能获取Body!");
//    private val body: ByteArray? by lazy {
//
//    }


//    val queryJson: JsonMap by lazy {
//        JsonMap.loadFromUrl(this.queryString ?: "")
//    }


    @Throws(IOException::class)
    override fun getReader(): BufferedReader {
        return BufferedReader(InputStreamReader(inputStream))
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


    @Throws(IOException::class)
    override fun getInputStream(): ServletInputStream {

        val bais: ByteArrayInputStream by lazy {
            //如果 10MB
            if (this.IsOctetContent) {
                throw RuntimeException("不能读取流二进制流!")
            }
            if (request.contentLength > config.maxHttpPostSize.toBytes()) {
                throw RuntimeException("请求体超过${(config.maxHttpPostSize.toString()).AsInt()}!")
            }
//        body_read = true;
            return@lazy request.inputStream.readBytes().inputStream()
        }

        return object : ServletInputStream() {

            @Throws(IOException::class)
            override fun read(): Int {
                return bais.read()
//                return request.inputStream.read()
            }

            override fun isFinished(): Boolean {
                return false
            }

            override fun isReady(): Boolean {
                return false
            }

            override fun setReadListener(readListener: ReadListener) {}
        }
    }


    fun getCookie(name: String): String = this.cookies?.firstOrNull { it.name == name }?.value ?: ""

}


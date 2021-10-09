package nbcp.web


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
    val body: ByteArray? by lazy {
        //如果 10MB
        if (this.IsOctetContent) {
            return@lazy null;
        }
        if (request.contentLength > config.maxHttpPostSize.toBytes()) {
            throw RuntimeException("请求体超过${(config.maxHttpPostSize.toString()).AsInt()}!")
        }
//        body_read = true;
        return@lazy request.inputStream.readBytes()
    }

    val json: JsonMap by lazy {
        var ret = JsonMap();


        if (request.contentType == null) {
            return@lazy ret;
        }
        if (request.contentType.startsWith(MediaType.APPLICATION_JSON_VALUE)) {
            var bodyString = (body ?: byteArrayOf()).toString(const.utf8).trim()

            if (bodyString.startsWith("{") && bodyString.endsWith("}")) {
                ret = bodyString.FromJsonWithDefaultValue();
            }
        } else if (request.contentType.startsWith(MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
            //按 key进行分组，假设客户端是：
            // corp[id]=1&corp[name]=abc&role[id]=2&role[name]=def
            //会分成两组 ret["corp"] = json1 , ret["role"] = json2;
            //目前只支持两级。不支持  corp[role][id]
            if (request.parameterNames.hasMoreElements()) {
                for (key in request.parameterNames) {
                    var value = request.getParameter(key);
                    var keyLastIndex = key.indexOf('[');
                    if (keyLastIndex >= 0) {
                        var mk = key.slice(0..keyLastIndex - 1);

                        setValue(ret, mk, key.substring(keyLastIndex), value);
                    } else {
                        setValue(ret, key, "", value);
                    }
                }
            } else {
                var bodyString = (body ?: byteArrayOf()).toString(const.utf8).trim()
                ret = JsonMap.loadFromUrl(bodyString)
            }
        }

        return@lazy ret;
    }

//    val queryJson: JsonMap by lazy {
//        JsonMap.loadFromUrl(this.queryString ?: "")
//    }


    private fun setValue(jm: JsonMap, prop: String, arykey: String, value: String) {
        if (arykey.isEmpty()) {
            jm[prop] = value;
            return;
        }

        var keyLastIndex = arykey.indexOf(']');
        var key = arykey.slice(1..keyLastIndex - 1);

        if (jm.containsKey(key) == false) {
            jm[key] = JsonMap();
        }

        setValue(jm[key] as JsonMap, key, arykey.substring(keyLastIndex + 1), value);
    }

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

        val bais by lazy {
            return@lazy ByteArrayInputStream(body);
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


    //从 json, queryString,header中获取值。
    fun getValue(key: String): String {
        if (queryJson.containsKey(key)) {
            var ret = queryJson.get(key)
            if (ret != null) {
                if (ret is String) return ret;
                return (ret as Collection<String>).joinToString(",")
            }

        }

        if (json.containsKey(key)) {
            var ret = json.get(key)
            if (ret != null) {
                if (ret is String) return ret;
                else if (ret is Collection<*>) return ret.joinToString(",")
                return ret.AsString()
            }
        }

        var ret = this.getHeader(key)
        if (ret != null) {
            return ret
        }

        return "";
    }

}


package nbcp.base.data

import nbcp.base.comm.StringMap
import nbcp.base.extend.AsString
import nbcp.base.extend.getByIgnoreCaseKey
import nbcp.base.utils.HttpUtil
import org.apache.http.HttpHeaders
import java.io.ByteArrayOutputStream

data class HttpRequestData @JvmOverloads constructor(
    var instanceFollowRedirects: Boolean = false,
    var useCaches: Boolean = false,
    var connectTimeout: Int = 5_000,
    var readTimeout: Int = 30_000,
    var chunkedStreamingMode: Int = 0,

    var requestMethod: String = "GET",
    var headers: StringMap = StringMap()
) {
    init {
        headers.set("Connection", "close")
    }

    var contentType: String
        get() {
            return this.headers.getByIgnoreCaseKey("Content-Type").AsString()
        }
        set(value) {
            this.headers["Content-Type"] = value;
        }


    /**
     * 在Header添加 AUTHORIZATION，使用 Basic $base64(用户名:密码) 格式
     */
    fun setAuthorization(userName: String, password: String) {
        this.headers.set(
            HttpHeaders.AUTHORIZATION,
            HttpUtil.getBasicAuthorization(userName, password)
        )
    }

    /**
     * postAction 是上传专用
     */
    var postAction: ((ByteArrayOutputStream) -> Unit)? = null

    /**
     * post 小数据量
     */
    var postBody = ""

    /**
     * 请求内容是否是文字
     */
    val postIsText: Boolean
        get() {
            return HttpUtil.getTextTypeFromContentType(this.contentType)
        }
}
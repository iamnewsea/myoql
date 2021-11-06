package nbcp.utils

import nbcp.comm.AsString
import nbcp.comm.StringMap
import java.io.DataOutputStream

data class HttpRequestData @JvmOverloads constructor(
    var instanceFollowRedirects: Boolean = false,
    var useCaches: Boolean = false,
    var connectTimeout: Int = 5_000,
    var readTimeout: Int = 30_000,
    var chunkedStreamingMode: Int = 0,

    var requestMethod: String = "",
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


    fun setAuthorization(userName: String, password: String) {
        this.headers.set(
            "Authorization",
            HttpUtil.getBasicAuthorization(userName, password)
        )
    }

    /**
     * postAction 是上传专用
     */
    var postAction: ((DataOutputStream) -> Unit)? = null

    /**
     * post 小数据量
     */
    var postBody = ""

    /**
     * 请求内容是否是文字
     */
    val postIsText: Boolean
        get() {
            return getTextTypeFromContentType(this.contentType)
        }
}
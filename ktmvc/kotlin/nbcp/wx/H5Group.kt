package nbcp.wx

import nbcp.base.extend.AsLocalDateTime
import nbcp.base.extend.HasValue
import nbcp.base.extend.Slice
import nbcp.base.utils.CodeUtil
import nbcp.comm.ApiResult
import nbcp.comm.utf8
import nbcp.db.db
import org.springframework.beans.factory.annotation.Value
import java.security.MessageDigest
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

object H5Group {
    @Value("\${app.wx.appId}")
    lateinit var appId: String

    /**
     *  wx.config 结构，其中不包含 jsApiList，jsApiList在客户端指定。
     *  H5分享用
     */
    fun getJsapiTicket(fullUrl: String,appSecret:String): ApiResult<JsapiTicketData> {

        var ticketResult = db.rer_base.wx.getJsapiTicket(appId, appSecret);
        if (ticketResult.msg.HasValue) {
            return ApiResult(ticketResult.msg);
        }
        if (ticketResult.data.isNullOrEmpty()) {
            return ApiResult("找不到 ticket");
        }

        var jsapiTicket = ticketResult.data ?: ""

        var wxInfo = JsapiTicketData(appId);
        wxInfo.fillSign(appSecret, jsapiTicket, fullUrl);
        return ApiResult.of(wxInfo)
    }
}



/**
 * https://developers.weixin.qq.com/doc/offiaccount/OA_Web_Apps/JS-SDK.html#54
 * wx.config 结构，其中不包含 jsApiList，jsApiList在客户端指定。
 */
data class JsapiTicketData(
        var appId: String = ""
) {

    var timestamp: Long = Duration.between("1970-01-01".AsLocalDateTime(), LocalDateTime.now()).seconds
    var nonceStr: String = CodeUtil.getCode().Slice(0, 32)
    var signature: String = ""


    fun fillSign(appSecret: String, jsapi_ticket: String, fullUrl: String) {
        val string1: String
        var signature = ""

        //注意这里参数名必须全部小写，且必须有序
        //注意这里参数名必须全部小写，且必须有序
        string1 = "jsapi_ticket=" + jsapi_ticket +
                "&noncestr=" + nonceStr +
                "&timestamp=" + timestamp +
                "&url=" + fullUrl

        val crypt = MessageDigest.getInstance("SHA-1")
        crypt.reset()
        crypt.update(string1.toByteArray(utf8))
        signature = byteToHex(crypt.digest())

        this.signature = signature;
    }

    private fun byteToHex(hash: ByteArray): String {
        val formatter = Formatter()
        for (b in hash) {
            formatter.format("%02x", b)
        }
        val result = formatter.toString()
        formatter.close()
        return result
    }
}
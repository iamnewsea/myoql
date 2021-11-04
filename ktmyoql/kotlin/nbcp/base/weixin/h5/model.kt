package nbcp.base.weixin.h5

import nbcp.comm.*
import nbcp.utils.CodeUtil
import nbcp.base.weixin.system.wx_return_data
import nbcp.base.weixin.wx
import java.io.Serializable
import java.security.MessageDigest
import java.time.Duration
import java.time.LocalDateTime
import java.util.*


/**
 * https://developers.weixin.qq.com/doc/offiaccount/OA_Web_Apps/JS-SDK.html#54
 * wx.config 结构，其中不包含 jsApiList，jsApiList在客户端指定。
 */
class JsapiTicketData : Serializable {
    var appId: String = wx.appId
    var timestamp: Long = Duration.between("1970-01-01".AsLocalDateTime(), LocalDateTime.now()).seconds
    var nonceStr: String = CodeUtil.getCode().Slice(0, 32)
    var signature: String = ""


    fun fillSign(appSecret: String, jsapi_ticket: String, fullUrl: String) {
        require(appSecret.HasValue) { "缺少appSecret!" }
        //注意这里参数名必须全部小写，且必须有序
        val string1 = "jsapi_ticket=" + jsapi_ticket +
                "&noncestr=" + nonceStr +
                "&timestamp=" + timestamp +
                "&url=" + fullUrl

        val crypt = MessageDigest.getInstance("SHA-1")
        crypt.reset()
        crypt.update(string1.toByteArray(const.utf8))

        this.signature = byteToHex(crypt.digest());
    }

    private fun byteToHex(hash: ByteArray): String {
        Formatter().use { formatter ->
            for (b in hash) {
                formatter.format("%02x", b)
            }
            return formatter.toString()
        }
    }
}

/**
 * 微信公众号用户信息
 * https://developers.weixin.qq.com/doc/offiaccount/User_Management/Get_users_basic_information_UnionID.html#UinonId
 */
class WxH5UserInfoData : wx_return_data() {
    var openid = ""
    var nickname = ""
    var sex = 0
    var language = ""
    var city = ""
    var province = ""
    var country = ""
    var headimgurl = ""
}
package nbcp.wx.pay

import nbcp.comm.AsLocalDateTime
import nbcp.comm.Slice
import nbcp.utils.CodeUtil
import nbcp.utils.SpringUtil
import java.time.Duration
import java.time.LocalDateTime


/**
 * 微信小程序调起支付时，签名使用的数据
 * https://pay.weixin.qq.com/wiki/doc/api/wxa/wxa_api.php?chapter=7_7&index=5
 */
class WxRequestPaymentServerSignData(prepay_id: String) {
    var appId: String = SpringUtil.context.environment.getProperty("app.wx.appId")
    var timeStamp: Long = Duration.between("1970-01-01".AsLocalDateTime(), LocalDateTime.now()).seconds
    var signType: String = "MD5"
    var nonceStr: String = CodeUtil.getCode().Slice(0, 32)
    var `package`: String = ""

    init {
        this.`package` = "prepay_id=${prepay_id}"
    }
}

/**
 * 微信小程序调起支付使用的数据
 * https://pay.weixin.qq.com/wiki/doc/api/wxa/wxa_api.php?chapter=7_7&index=5
 */
data class WxRequestPaymentData(
        var timeStamp: Long = 0L,
        var signType: String = "",
        var nonceStr: String = "",
        var `package`: String = "",
        var paySign: String = ""
) {
}

/**
 * 预支付Id的返回结果
 * 返回的参数很多，有很多是和请求参数相同的， 为了简单，只定义重要的返回参数
 * trade_type=NATIVE时，也会返回  二维码链接code_urldetailByWxOpenId
 */
data class WxPrePayServerResponseData(
        var return_code: String = "",   //SUCCESS/FAIL
        var return_msg: String = "",


        var result_code: String = "",    //SUCCESS/FAIL
        var err_code: String = "",
        var err_code_des: String = "",

        var trade_type: String = "",   //交易类型
        var prepay_id: String = ""      //预支付交易会话标识
)
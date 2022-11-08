package nbcp.myoql.weixin.pay

import nbcp.base.comm.*;
import nbcp.base.db.*;
import nbcp.base.enums.*;
import nbcp.base.extend.*;
import nbcp.base.utils.*;
import nbcp.myoql.db.enums.*
import nbcp.myoql.db.*;
import nbcp.myoql.db.comm.*
import nbcp.myoql.weixin.wx
import java.time.Duration
import java.time.LocalDateTime


/**
 * 微信小程序调起支付时，签名使用的数据
 * https://pay.weixin.qq.com/wiki/doc/api/wxa/wxa_api.php?chapter=7_7&index=5
 */
class WxRequestPaymentServerSignData(prepay_id: String) {
    var appId: String = wx.appId
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
data class WxRequestPaymentData @JvmOverloads constructor(
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
data class WxPrePayServerResponseData @JvmOverloads constructor(
        var return_code: String = "",   //SUCCESS/FAIL
        var return_msg: String = "",


        var result_code: String = "",    //SUCCESS/FAIL
        var err_code: String = "",
        var err_code_des: String = "",

        var trade_type: String = "",   //交易类型
        var prepay_id: String = ""      //预支付交易会话标识
)
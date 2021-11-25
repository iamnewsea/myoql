package nbcp.base.weixin.pay

import nbcp.comm.*
import nbcp.utils.CodeUtil
import nbcp.utils.HttpUtil
import nbcp.base.weixin.wx
import java.lang.RuntimeException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 微信支付，获取预支付Id
 * https://pay.weixin.qq.com/wiki/doc/api/wxa/wxa_api.php?chapter=9_1&index=1
 *
 * 设计思路：
 * 微信请求参数做实体。
 * 主要参数做为实体初始化参数。
 * 微信请求做成实体方法。
 */
data class WxPrePayServerRequestData @JvmOverloads constructor(
    var body: String,
    var spbill_create_ip: String,// 终端IP
    var notify_url: String,
    var out_trade_no: String,
    var total_fee: Int,
    var openid: String,
    var attach: String = "", // 附加数据
    var detail: String = ""  // 商品详情
) {
    var appid: String = wx.appId
    var mch_id: String = wx.mchId

    private var sign_type: String = "MD5"

    //    @Ignore
    //  var sign: String = ""


    private var nonce_str: String = CodeUtil.getCode().Slice(0, 32)  //是

    //private var fee_type: String = "CNY" //否
    private var time_start: String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) //否
    private var time_expire: String =
        LocalDateTime.now().plusMinutes(30).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) //否
    //private var limit_pay: String = "no_credit" // 指定支付方式


    var trade_type: String = "JSAPI" //是 , APP , JSAPI 两种
    var device_info: String = "WEB" //否
    //var product_id: String = "" //否

    //微信小程序端支付
    fun toWxAppPayXml(mchSecret: String): String {
        this.trade_type = "JSAPI"
        return wx.sys.toXml(mchSecret, this);
    }


    /**
     * 获取预付Id
     */
    fun getPrepayId(mchSecret: String): ApiResult<String> {
        val url = HttpUtil("https://api.mch.weixin.qq.com/pay/unifiedorder")
        url.request. contentType = "text/xml;charset=UTF-8"

        val result = url.doPost(this.toWxAppPayXml(mchSecret))
            .Xml2Json()
            .get("xml")
            ?.ConvertJson(WxPrePayServerResponseData::class.java)

        if (result == null) {
            return ApiResult.error("请求中出错!")
        }

        if (result.return_code != "SUCCESS" && result.result_code != "SUCCESS") {
            throw RuntimeException("获取PrepayId错误:" + result.return_msg.AsString(result.err_code_des));
        }

        return ApiResult.of(result.prepay_id)
    }
}

package nbcp.wx

import nbcp.base.extend.*
import nbcp.base.utils.CodeUtil
import nbcp.base.utils.HttpUtil
import nbcp.base.utils.Md5Util
import nbcp.base.utils.SpringUtil
import nbcp.comm.ApiResult
import nbcp.comm.Ignore
import nbcp.comm.JsonResult
import nbcp.comm.Require
import nbcp.web.ClientIp
import nbcp.web.HttpContext
import org.apache.http.ssl.SSLContexts
import java.io.InputStream
import java.lang.RuntimeException
import java.security.KeyStore
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.net.ssl.SSLSocketFactory

object WxPayGroup {

    /**
     * 填充 paySign 进行签名
     * https://pay.weixin.qq.com/wiki/doc/api/wxa/wxa_api.php?chapter=4_3
     */
    fun getPayClientSignData(mchSecret: String, prepay_id: String): WxRequestPaymentData {
        var signData = WxRequestPaymentServerSignData(prepay_id);

        //var type = this::class.java;
        //二次签名
        var paySign = wx.sign(mchSecret, signData);
        return WxRequestPaymentData(signData.timeStamp, signData.signType, signData.nonceStr, signData.`package`, paySign);
    }

    /**
     * 微信支付回调的应答
     * https://pay.weixin.qq.com/wiki/doc/api/H5.php?chapter=9_7&index=8
     * @param msg: 错误消息。
     */
    fun payReplyWeixin(msg: String = ""): String {
        var code = if (msg.HasValue) "FAIL" else "SUCCESS"

        return """<xml>
<return_code><![CDATA[${code}]]></return_code>
<return_msg><![CDATA[${msg.AsString("OK")}]]></return_msg>
</xml>"""
    }


    /**
     * 微信支付，获取预支付Id
     * https://pay.weixin.qq.com/wiki/doc/api/wxa/wxa_api.php?chapter=9_1&index=1
     *
     * 设计思路：
     * 微信请求参数做实体。
     * 主要参数做为实体初始化参数。
     * 微信请求做成实体方法。
     */
    data class WxPrePayServerRequestData(
            var notify_url: String = "",
            var out_trade_no: String = "",
            var total_fee: Int = 0,
            var openid: String = "",
            var attach: String = "", // 附加数据
            var detail: String = ""  // 商品详情
    ) {
        var appid: String = SpringUtil.context.environment.getProperty("server.wx.appId")
        var mch_id: String = SpringUtil.context.environment.getProperty("server.wx.mchId")

        var spbill_create_ip: String = HttpContext.request.ClientIp  // 终端IP
        private var sign_type: String = "MD5"

        //    @Ignore
        //  var sign: String = ""
        var body: String = "时光锁纪-相册"

        private var nonce_str: String = CodeUtil.getCode().Slice(0, 32)  //是

        //private var fee_type: String = "CNY" //否
        private var time_start: String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) //否
        private var time_expire: String = LocalDateTime.now().plusMinutes(30).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) //否
        //private var limit_pay: String = "no_credit" // 指定支付方式


        var trade_type: String = "JSAPI" //是 , APP , JSAPI 两种
        var device_info: String = "WEB" //否
        //var product_id: String = "" //否

        //微信小程序端支付
        fun toWxAppPayXml(mchSecret: String): String {
            this.trade_type = "JSAPI"
            return wx.toXml(mchSecret, this);
        }


        /**
         * 获取预付Id
         */
        fun getPrepayId(mchSecret: String): ApiResult<String> {
            val url = HttpUtil("https://api.mch.weixin.qq.com/pay/unifiedorder")
            url.requestHeader["Content-Type"] = "text/xml;charset=UTF-8"
            val result = url.doPost(this.toWxAppPayXml(mchSecret))
                    .Xml2Json()
                    .get("xml")
                    ?.ConvertJson(WxPrePayServerResponseData::class.java)

            if (result == null) {
                return ApiResult("请求中出错!")
            }

            if (result.return_code != "SUCCESS" && result.result_code != "SUCCESS") {
                throw RuntimeException("获取PrepayId错误:" + result.return_msg.AsString(result.err_code_des));
            }

            return ApiResult.of(result.prepay_id)
        }
    }

    /**
     * 预付
     */
    fun prepay(
            notify_url: String = "",
            out_trade_no: String = "",
            total_fee: Int = 0,
            openid: String = "",
            attach: String = "", // 附加数据
            detail: String = ""  // 商品详情
    ) = WxPrePayServerRequestData(
            notify_url,
            out_trade_no,
            total_fee,
            openid,
            attach, // 附加数据
            detail  // 商品详情
    )

}


/**
 * 微信小程序调起支付时，签名使用的数据
 * https://pay.weixin.qq.com/wiki/doc/api/wxa/wxa_api.php?chapter=7_7&index=5
 */
class WxRequestPaymentServerSignData(prepay_id: String) {
    var appId: String = SpringUtil.context.environment.getProperty("server.wx.appId")
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
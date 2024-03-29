package nbcp.wx.pay

import nbcp.comm.*
import nbcp.utils.CodeUtil
import nbcp.utils.HttpUtil
import nbcp.utils.Md5Util
import nbcp.utils.SpringUtil
import nbcp.wx.wx
import java.lang.RuntimeException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object WxPayGroup {

    /**
     * 企业付款给个人
     */
    fun corpPayToUser(
            spbill_create_ip: String,
            amount: Int = 0,
            openid: String,  //用户openid
            partner_trade_no: String, // 商户订单号，需保持唯一性
            desc: String   // 企业付款备注
    ) = WxCorpPayToUserData(spbill_create_ip, amount, openid, partner_trade_no, desc)

    /**
     * 退款
     */
    fun refundPay(
            out_trade_no: String,//是     商户订单号
            out_refund_no: String,   //商户退款单号
            total_fee: Int, //是, 订单总金额
            refund_fee: Int,//是        退款金额
            refund_desc: String = ""   //退款原因   否
    ) = WxRefundPayRequestData(out_trade_no, out_refund_no, total_fee, refund_fee, refund_desc)


    /**
     * 填充 paySign 进行签名
     * https://pay.weixin.qq.com/wiki/doc/api/wxa/wxa_api.php?chapter=4_3
     */
    fun getPayClientSignData(mchSecret: String, prepay_id: String): WxRequestPaymentData {
        var signData = WxRequestPaymentServerSignData(prepay_id);

        //var type = this::class.java;
        //二次签名
        var paySign = wx.sys.sign(mchSecret, signData);
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
     * 预付
     */
    fun prepay(
            body:String,
            spbill_create_ip: String,
            notify_url: String,
            out_trade_no: String,
            total_fee: Int,
            openid: String,
            attach: String = "", // 附加数据
            detail: String = ""  // 商品详情
    ) = WxPrePayServerRequestData(
            body,
            spbill_create_ip,
            notify_url,
            out_trade_no,
            total_fee,
            openid,
            attach, // 附加数据
            detail  // 商品详情
    )
}
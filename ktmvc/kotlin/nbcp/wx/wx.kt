package nbcp.wx

import nbcp.base.extend.AllFields
import nbcp.base.utils.Md5Util
import nbcp.comm.Ignore
import nbcp.comm.Require
import java.lang.RuntimeException

/**
 * 微信助手类
 */
object wx {

    /**
     * 微信签名
     * https://pay.weixin.qq.com/wiki/doc/api/wxa/wxa_api.php?chapter=4_3
     * 忽略 @Ignore 字段， 一般是 sign 字段。
     * 如果 指定了 @Require ，则要求不能为空。
     */
    fun sign(mchSecret: String, wxModel: Any): String {
        var type = wxModel::class.java
        var list = type.AllFields
                .sortedBy { it.name }
                .map {
                    if (it.getAnnotation(Ignore::class.java) != null) {
                        return@map ""
                    }

                    var require = it.getAnnotation(Require::class.java) != null

                    var value = it.get(wxModel)

                    if (value == null) {
                        if (require) {
                            throw RuntimeException("微信小程序签名时，${it.name} 不能为空值");
                        }
                        return@map "";
                    }
                    if (value is String && value.isNullOrEmpty()) {
                        if (require) {
                            throw RuntimeException("微信小程序签名时，${it.name} 为必填项");
                        }
                        return@map ""
                    }

                    return@map it.name + "=" + value
                }
                .filter { it.isNotEmpty() }
                .toMutableList();

        list.add("key=${mchSecret}");
        return Md5Util.getMd5(list.joinToString("&")).toUpperCase();
    }


    fun toXml(mchSecret: String, wxModel: Any): String {
        var sign = sign(mchSecret, wxModel);
        if (sign.isEmpty()) return "";

        var type = wxModel::class.java;

        return "<xml>" + type.AllFields
                .sortedBy { it.name }
                .map {

                    var value = it.get(wxModel)
                    if (value == null) {
                        return@map "" to ""
                    }

                    if (value is String && value.isEmpty()) {
                        return@map "" to ""
                    }

                    return@map it.name to value
                }
                .filter { it.first.isNotEmpty() }
                .toMutableList()
                .apply {
                    add("sign" to sign)
                }
                .map {
                    return@map "<${it.first}><![CDATA[${it.second}]]></${it.first}>"
                }
                .joinToString("") + "</xml>";
    }


    val pay = WxPayGroup
    val user = WxUserGroup


    /**
     * 企业付款给个人
     */
    fun corpPayToUser(
            amount: Int = 0,
            openid: String  ,  //用户openid
            partner_trade_no: String  , // 商户订单号，需保持唯一性
            desc: String   // 企业付款备注
    ) = WxCorpPayToUserData(amount, openid, partner_trade_no, desc)

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

    val sys = WxSystemGroup

}
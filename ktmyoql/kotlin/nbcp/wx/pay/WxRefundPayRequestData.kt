package nbcp.wx.pay

import nbcp.comm.*
import nbcp.utils.*
import nbcp.wx.wx
import org.apache.http.ssl.SSLContexts
import org.springframework.core.io.ClassPathResource
import java.security.KeyStore
import javax.net.ssl.SSLSocketFactory

/**
 * 退款
 * https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=9_4
 */
data class WxRefundPayRequestData @JvmOverloads constructor(
    @Require
    var out_trade_no: String,//是     商户订单号
    var out_refund_no: String,   //商户退款单号

    @Require
    var total_fee: Int, //是, 订单总金额
    var refund_fee: Int,//是        退款金额

    var refund_desc: String = ""   //退款原因   否

) {
    var appid: String = wx.appId
    var mch_id: String = wx.mchId

    var notify_url: String = ""   //退款结果通知url   否
    var refund_fee_type: String = ""     //退款货币种类   否
    var refund_account: String = ""    //退款资金来源   否
    private var nonce_str: String = CodeUtil.getCode().Slice(0, 32)  //是

//    @Ignore
//    private var sign: String = ""

    private var sign_type: String = "MD5" //否

    private fun getSSLSocketFactory(mchId: String): SSLSocketFactory {
        /**
         * 注意PKCS12证书 是从微信商户平台-》账户设置-》 API安全 中下载的
         */
        //SSL认证
        val keyStore = KeyStore.getInstance("PKCS12")
//        val instream = FileInputStream(File("C:/Users/udi/Desktop/cert/apiclient_cert.p12"))//P12文件目录

        ClassPathResource("wx-mch-cert/apiclient_cert.p12").inputStream.use { wx_cert ->
            keyStore.load(wx_cert, mchId.toCharArray())    //证书数和密码写入
        }

        //创建向客户端提交的证书
        val sslcontext = SSLContexts.custom()
            .loadKeyMaterial(keyStore, mchId.toCharArray()) //密码
            .build()


        return sslcontext.socketFactory;
        // 只允许使用TLSv1协议  及   自己的证书
//        return SSLConnectionSocketFactory(
//                sslcontext.socketFactory,
//                arrayOf("TLSv1"),
//                null,
//                BrowserCompatHostnameVerifier.INSTANCE)

    }

    /**
     * 发起退款（仅呼叫微信接口，不更改数据库） 有待测试 by yuxinhai at 2020/2/22
     */
    fun doRefundPay(mchSecret: String): JsonResult {
        var url = "https://api.mch.weixin.qq.com/secapi/pay/refund"

        val http = HttpUtil(url)
        http.request.contentType = "text/xml;charset=UTF-8"

        var postData = wx.sys.toXml(mchSecret, this);

        http.sslSocketFactory = getSSLSocketFactory(mch_id)

        val result = http.doPost(postData)
            .Xml2Json()
            .get("xml")
            ?.ConvertJson(WxRefundPayResponseData::class.java)
            ?: return JsonResult("请求中出错!");

        if (result.return_code != "SUCCESS" && result.result_code != "SUCCESS") {
            return JsonResult("退款错误:" + result.return_msg.AsString(result.err_code_des));
        }

        return JsonResult()
    }
}


/**
 * 返回的参数很多，有很多是和请求参数相同的， 为了简单，只定义重要的返回参数
 */
data class WxRefundPayResponseData @JvmOverloads constructor(
    var return_code: String = "",       //SUCCESS/FAIL
    var return_msg: String = "",


    var result_code: String = "",       //SUCCESS/FAIL
    var err_code: String = "",
    var err_code_des: String = "",
    var out_trade_no: String = "",
    var out_refund_no: String = "",
    var refund_fee: Int = 0,  //退款金额
    var settlement_refund_fee: Int = 0,  //应结退款金额
    var total_fee: Int = 0,   //标价金额
    var settlement_total_fee: Int = 0  //应结订单金额
)
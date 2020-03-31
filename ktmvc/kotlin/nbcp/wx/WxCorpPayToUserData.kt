package nbcp.wx

import nbcp.base.extend.AsString
import nbcp.base.extend.ConvertJson
import nbcp.base.extend.Slice
import nbcp.base.extend.Xml2Json
import nbcp.base.utils.CodeUtil
import nbcp.base.utils.HttpUtil
import nbcp.base.utils.SpringUtil
import nbcp.comm.Ignore
import nbcp.comm.JsonResult
import nbcp.web.ClientIp
import nbcp.web.HttpContext
import org.apache.http.ssl.SSLContexts
import java.io.InputStream
import java.security.KeyStore
import javax.net.ssl.SSLSocketFactory

/**
 * 微信企业付款
 * https://pay.weixin.qq.com/wiki/doc/api/tools/mch_pay.php?chapter=14_2
 */
class WxCorpPayToUserData(
        var amount: Int = 0,
        var openid: String = "",  //用户openid
        var partner_trade_no: String = "", // 商户订单号，需保持唯一性
        var desc: String = ""  // 企业付款备注
) {
    var mch_appid: String = SpringUtil.context.environment.getProperty("app.wx.appId")
    var mchid: String = SpringUtil.context.environment.getProperty("app.wx.mchId")
    var spbill_create_ip: String = HttpContext.request.ClientIp   // 终端IP
    //var spbill_create_ip: String = "60.220.69.11"   // 终端IP
    var check_name: String = "NO_CHECK"  //NO_CHECK  不校验真实姓名   FORCE_CHECK：强校验真实姓名
    //var sign_type: String = "MD5"   不需要  居然是因为不需要

    @Ignore
    var sign: String = ""

    var nonce_str: String = CodeUtil.getCode().Slice(0, 32)  //是

    private fun getSSLSocketFactory( pkcs12FilePath: InputStream): SSLSocketFactory {
        /**
         * 注意PKCS12证书 是从微信商户平台-》账户设置-》 API安全 中下载的
         */
        //SSL认证
        val keyStore = KeyStore.getInstance("PKCS12")

        pkcs12FilePath.use { wx_cert ->
            keyStore.load(wx_cert, mchid.toCharArray())    //证书数和密码写入
        }

        //创建向客户端提交的证书
        val sslcontext = SSLContexts.custom()
                .loadKeyMaterial(keyStore, mchid.toCharArray()) //密码
                .build()
        return sslcontext.socketFactory;
    }


    /**
     *  用于企业向微信用户个人付款,发送到零钱
     *  @param pkcs12FilePath: 如果是打到资源包中，使用： ClassPathResource("wx-mch-cert/apiclient_cert.p12").inputStream
     */
    fun payToPerson(mchSecret: String,pkcs12FilePath: InputStream): JsonResult {
        var url = "https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers"
        val http = HttpUtil(url)
        http.requestHeader["Content-Type"] = "text/xml;charset=UTF-8"
        var postData = wx.toXml(mchSecret, this);

        http.setRequest {
            (it as javax.net.ssl.HttpsURLConnection)
                    .setSSLSocketFactory(getSSLSocketFactory( pkcs12FilePath))
        }

        val result = http.doPost(postData)
                .Xml2Json()
                .get("xml")
                ?.ConvertJson(WxRefundPayResponseData::class.java)
                ?: return JsonResult("请求中出错!");

        if (result.return_code != "SUCCESS" && result.result_code != "SUCCESS") {
            return JsonResult("发送红包出错:" + result.return_msg.AsString(result.err_code_des));
        }

        if (result.return_msg == "NO_AUTH"){
            return JsonResult("发送红包出错:" + result.return_msg.AsString(result.err_code_des));
        }

        return JsonResult()
    }
}
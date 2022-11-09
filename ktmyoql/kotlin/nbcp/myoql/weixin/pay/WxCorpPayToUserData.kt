package nbcp.myoql.weixin.pay

import nbcp.base.comm.JsonResult
import nbcp.base.extend.AsString
import nbcp.base.extend.ConvertJson
import nbcp.base.extend.Slice
import nbcp.base.extend.Xml2Json
import nbcp.base.utils.CodeUtil
import nbcp.base.utils.HttpUtil
import nbcp.myoql.weixin.wx
import org.apache.http.ssl.SSLContexts
import java.io.InputStream
import java.security.KeyStore
import javax.net.ssl.SSLSocketFactory

/**
 * 微信企业付款
 * https://pay.weixin.qq.com/wiki/doc/api/tools/mch_pay.php?chapter=14_2
 */
class WxCorpPayToUserData @JvmOverloads constructor(
    var spbill_create_ip: String, // 终端IP
    var amount: Int,
    var openid: String,  //用户openid
    var partner_trade_no: String, // 商户订单号，需保持唯一性
    var desc: String = ""  // 企业付款备注
) {
    var mch_appid: String = wx.appId
    var mchid: String = wx.mchId

    //var spbill_create_ip: String = "60.220.69.11"   // 终端IP
    var check_name: String = "NO_CHECK"  //NO_CHECK  不校验真实姓名   FORCE_CHECK：强校验真实姓名
    //var sign_type: String = "MD5"   不需要  居然是因为不需要

    @nbcp.base.comm.Ignore
    var sign: String = ""

    var nonce_str: String = CodeUtil.getCode().Slice(0, 32)  //是

    private fun getSSLSocketFactory(pkcs12FilePath: InputStream): SSLSocketFactory {
        /**
         * 注意 PKCS12 证书 是从微信商户平台-》账户设置-》 API安全 中下载的
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
    fun payToPerson(mchSecret: String, pkcs12FilePath: InputStream): JsonResult {
        val url = "https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers"
        val http = HttpUtil(url)
        http.request.contentType = "text/xml;charset=UTF-8"


        val postData = wx.sys.toXml(mchSecret, this);

        http.sslSocketFactory = getSSLSocketFactory(pkcs12FilePath);

        val result = http.doPost(postData)
            .apply {
                if( http.isError){
                    return JsonResult.error("接口调用出错",http.status)
                }
            }
            .Xml2Json()
            .get("xml")
            ?.ConvertJson(WxRefundPayResponseData::class.java)
            ?: return JsonResult.error("接口返回数据错误!");

        if (result.return_code != "SUCCESS" && result.result_code != "SUCCESS") {
            return JsonResult.error("发送红包出错:" + result.return_msg.AsString(result.err_code_des));
        }

        if (result.return_msg == "NO_AUTH") {
            return JsonResult.error("发送红包出错:" + result.return_msg.AsString(result.err_code_des));
        }

        return JsonResult()
    }
}
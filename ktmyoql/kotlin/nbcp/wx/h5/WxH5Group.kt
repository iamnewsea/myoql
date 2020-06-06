package nbcp.wx.h5

import nbcp.comm.ApiResult
import nbcp.comm.HasValue
import nbcp.db.db
import nbcp.wx.wx
import org.springframework.beans.factory.annotation.Value

/**
 * 开放平台相关内容
 */
object WxH5Group {

    @Value("\${app.wx.appId}")
    lateinit var appId: String

    /**
     *  wx.config 结构，其中不包含 jsApiList，jsApiList在客户端指定。
     *  H5分享用
     */
    fun getJsapiTicket(fullUrl: String, appSecret: String): ApiResult<JsapiTicketData> {

        var ticketResult = wx.officeAccount.getJsapiTicket(appId, appSecret);
        if (ticketResult.msg.HasValue) {
            return ApiResult(ticketResult.msg);
        }
        if (ticketResult.data.isNullOrEmpty()) {
            return ApiResult("找不到 ticket");
        }

        var jsapiTicket = ticketResult.data ?: ""

        var wxInfo = JsapiTicketData(appId);
        wxInfo.fillSign(appSecret, jsapiTicket, fullUrl);
        return ApiResult.of(wxInfo)
    }

    /**
     * H5 登录，使用 code 换 access_token
     * https://developers.weixin.qq.com/doc/oplatform/Website_App/WeChat_Login/Wechat_Login.html
     */
    fun getH5LoginAccessToken(appSecret: String, code: String): String {
        var url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=${appId}&secret=${appSecret}&code=${code}&grant_type=authorization_code"

        //TODO  未完待续
        return "";
    }
}
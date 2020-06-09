package nbcp.wx.h5

import nbcp.comm.*
import nbcp.db.db
import nbcp.utils.HttpUtil
import nbcp.utils.SpringUtil
import nbcp.wx.wx
import org.springframework.beans.factory.annotation.Value

/**
 * 开放平台相关内容
 */
object WxH5Group {
    /**
     *  wx.config 结构，其中不包含 jsApiList，jsApiList在客户端指定。
     *  H5分享用
     */
    fun getJsapiTicket(fullUrl: String, appSecret: String): ApiResult<JsapiTicketData> {

        var ticketResult = wx.officeAccount.getJsapiTicket(wx.appId, appSecret);
        if (ticketResult.msg.HasValue) {
            return ApiResult(ticketResult.msg);
        }
        if (ticketResult.data.isNullOrEmpty()) {
            return ApiResult("找不到 ticket");
        }

        var jsapiTicket = ticketResult.data ?: ""

        var wxInfo = JsapiTicketData(wx.appId);
        wxInfo.fillSign(appSecret, jsapiTicket, fullUrl);
        return ApiResult.of(wxInfo)
    }


    data class H5AccessTokenData(
            var access_token: String = "",
            var expires_in: Int = 0,
            var refresh_token: String = "",
            var openid: String = "",
            var scope: String = "",
            var unionid: String = "",
            var errcode: Int = 0,
            var errmsg: String = ""
    )

    /**
     * H5 登录，使用 code 换 access_token
     * https://developers.weixin.qq.com/doc/oplatform/Website_App/WeChat_Login/Wechat_Login.html
     */
    fun getH5LoginAccessToken(appSecret: String, code: String): ApiResult<H5AccessTokenData> {
        var url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=${wx.appId}&secret=${appSecret}&code=${code}&grant_type=authorization_code"
        var ret = ApiResult<H5AccessTokenData>();
        var http = HttpUtil(url);
        var data = http.doGet().FromJson<H5AccessTokenData>() ?: H5AccessTokenData();
        if (data.errcode != 0) {
            ret.msg = data.errmsg;
            if (ret.msg.HasValue) {
                return ret;
            }
        }
        ret.data = data;
        return ret;
    }
}
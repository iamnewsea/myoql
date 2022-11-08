package nbcp.myoql.weixin.h5

import nbcp.base.comm.*;
import nbcp.base.db.*;
import nbcp.base.enums.*;
import nbcp.base.extend.*;
import nbcp.base.utils.*;
import nbcp.myoql.db.enums.*
import nbcp.myoql.db.*;
import nbcp.myoql.db.comm.*
import nbcp.myoql.weixin.system.wx_return_data
import nbcp.myoql.weixin.wx

/**
 * 开放平台相关内容
 */
object WxH5Group {
    /**
     *  wx.config 结构，其中不包含 jsApiList，jsApiList在客户端指定。
     *  H5分享用
     */
    fun getJsapiTicket(fullUrl: String, appSecret: String): nbcp.base.comm.ApiResult<JsapiTicketData> {
        require(appSecret.HasValue) { "缺少appSecret!" }
        var ticketResult = wx.officeAccount.getJsapiTicket(appSecret);
        if (ticketResult.msg.HasValue) {
            return nbcp.base.comm.ApiResult.error(ticketResult.msg);
        }
        if (ticketResult.data.isNullOrEmpty()) {
            return nbcp.base.comm.ApiResult.error("找不到 ticket");
        }

        var jsapiTicket = ticketResult.data ?: ""

        var wxInfo = JsapiTicketData();
        wxInfo.fillSign(appSecret, jsapiTicket, fullUrl);
        return nbcp.base.comm.ApiResult.of(wxInfo)
    }


    class H5AccessTokenData @JvmOverloads constructor(
            var access_token: String = "",
            var expires_in: Int = 0,
            var refresh_token: String = "",
            var openid: String = "",
            var scope: String = "",
            var unionid: String = ""
    ): wx_return_data(){

    }

    /**
     * H5 登录，使用 code 换 access_token
     * https://developers.weixin.qq.com/doc/oplatform/Website_App/WeChat_Login/Wechat_Login.html
     */
    fun getH5LoginAccessToken(appSecret: String, code: String): nbcp.base.comm.ApiResult<H5AccessTokenData> {
        require(appSecret.HasValue) { "缺少appSecret!" }

        var url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=${wx.appId}&secret=${appSecret}&code=${code}&grant_type=authorization_code"
        var ret = nbcp.base.comm.ApiResult.error<H5AccessTokenData>("异常");
        var http = HttpUtil(url);
        var data = http.doGet().FromJson<H5AccessTokenData>() ?: H5AccessTokenData();
        if( http.isError){
            ret.code = http.status;
            ret.msg = "接口调用出错!"
            return ret;
        }
        if (data.errcode != 0) {
            ret.msg = data.errmsg;
            if (ret.msg.HasValue) {
                return ret;
            }
        }
        ret.data = data;
        return ret;
    }


    /**
     * https://developers.weixin.qq.com/doc/offiaccount/User_Management/Get_users_basic_information_UnionID.html#UinonId
     */
    fun getH5UserInfo(access_token: String, openid: String): nbcp.base.comm.ApiResult<WxH5UserInfoData> {
        require(access_token.HasValue) { "缺少access_token" }
        require(openid.HasValue) { "缺少openid" }

        var url = "https://api.weixin.qq.com/cgi-bin/user/info?access_token=${access_token}&openid=${openid}&lang=zh_CN"
        var ret = nbcp.base.comm.ApiResult<WxH5UserInfoData>();
        var http = HttpUtil(url);
        var data = http.doGet().FromJson<WxH5UserInfoData>() ?: WxH5UserInfoData();
        if( http.isError){
            ret.code = http.status;
            ret.msg = "接口调用出错!"
            return ret;
        }
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
package nbcp.wx


data class wx_access_token(
        var appId: String = "",
        var token: String = "",
        var expires_in: Int = 7200
)

/**
 * https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/login/auth.code2Session.html
 * wx.login 返回 code ,通过上述URL返回的数据
 */
data class WxLoginInfoModel(
        var openid: String = "",
        var session_key: String = "",
        var unionid: String = "",
        var errcode: String = "",
        var errmsg: String = ""
)


package nbcp.wx.miniprogram

import java.time.LocalDateTime


/**
 * 微信小程序所需要的用户信息。 小程序调用 wx.getUserInfo 返回的结构。
 * https://developers.weixin.qq.com/miniprogram/dev/api/open-api/user-info/wx.getUserInfo.html
 */
data class WxMiniProgramUserData @JvmOverloads constructor(
//        var code: String = "",  //微信登录时产生的临时Code，用于验证用户登录信息

        var session_key: String = "",
        var openid: String = "",
        var unionid: String = "",

        var access_token: String = "",
        var expires_in: Int = 0,
        var refresh_token: String = "",

        var nickName: String = "",
        var gender: String = "",

        var phoneNumber: String = "",
        var city: String = "",
        var province: String = "",
        var country: String = "",
        var avatarUrl: String = "",
        var createAt: LocalDateTime = LocalDateTime.now()
)

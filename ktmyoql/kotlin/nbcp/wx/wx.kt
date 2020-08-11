package nbcp.wx

import nbcp.comm.config
import nbcp.utils.SpringUtil
import nbcp.wx.h5.WxH5Group
import nbcp.wx.miniprogram.WxMiniProgramGroup
import nbcp.wx.officeaccount.WxOfficeAccountGroup
import nbcp.wx.pay.WxPayGroup
import nbcp.wx.system.WxSystemGroup
import java.time.LocalDateTime

/**
 * 微信的用户信息。
 */
data class WxUserData(
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

object wx {

    val appId get() = config.wxAppId
    val mchId get() = config.wxMchId


    val h5 = WxH5Group
    val officeAccount = WxOfficeAccountGroup
    val miniProgram = WxMiniProgramGroup
    val pay = WxPayGroup
    val sys = WxSystemGroup
}

package nbcp.wx

import nbcp.wx.h5.WxH5Group
import nbcp.wx.miniprogram.WxMiniProgramGroup
import nbcp.wx.officeaccount.WxOfficeAccountGroup
import nbcp.wx.pay.WxPayGroup
import nbcp.wx.system.WxSystemGroup

/**
 * 微信的用户信息。
 */
data class WxUserData(
        var session_key: String = "",
        var openid: String = "",
        var unionid: String = "",

        var access_token:String = "",
        var expires_in:String = "",
        var refresh_token:String = "",

        var nickName: String = "",
        var gender: String = "",

        var phoneNumber: String = "",
        var city: String = "",
        var province: String = "",
        var country: String = "",
        var avatarUrl: String = ""
)

object wx {
    val h5 = WxH5Group
    val officeAccount = WxOfficeAccountGroup
    val miniProgram = WxMiniProgramGroup
    val pay = WxPayGroup
    val sys = WxSystemGroup
}
package nbcp.wx

import nbcp.comm.config
import nbcp.db.UserSexEnum
import nbcp.utils.SpringUtil
import nbcp.wx.h5.WxH5Group
import nbcp.wx.h5.WxH5UserInfoData
import nbcp.wx.miniprogram.WxMiniProgramGroup
import nbcp.wx.miniprogram.WxMiniProgramUserData
import nbcp.wx.officeaccount.WxOfficeAccountGroup
import nbcp.wx.pay.WxPayGroup
import nbcp.wx.system.WxSystemGroup
import java.time.LocalDateTime

/**
 * 微信的用户信息。它是自定义综合体。
 * 结合了公众号和小程序用户信息的公共部分
 * 公众号：https://developers.weixin.qq.com/doc/offiaccount/User_Management/Get_users_basic_information_UnionID.html#UinonId
 * 和
 * 小程序：https://developers.weixin.qq.com/miniprogram/dev/api/open-api/user-info/UserInfo.html
 */
data class WxUserData @JvmOverloads constructor(
    var openid: String = "",
    var access_token: String = "",
    var nickName: String = "",

    var sex: UserSexEnum? = null, //性别
    var logoUrl: String = "",    //头像
    var city: String = "",
    var province: String = "",
    var country: String = "",

    /**
     * 获取手机号
     * https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/getPhoneNumber.html
     */
    var phoneNumber: String = ""
) {
    companion object {
        /**
         * 我真想用中文表示 公众号
         */
        @JvmOverloads
        fun fromH5UserInfo(h5UserInfo: WxH5UserInfoData, access_token: String = ""): WxUserData {
            val ret = WxUserData()
            ret.access_token = access_token;
            ret.openid = h5UserInfo.openid
            ret.nickName = h5UserInfo.nickname;
            if (h5UserInfo.sex == 1) {
                ret.sex = UserSexEnum.Male;
            } else if (h5UserInfo.sex == 2) {
                ret.sex = UserSexEnum.Female;
            }
            ret.city = h5UserInfo.city;
            ret.province = h5UserInfo.province
            ret.country = h5UserInfo.country;
            ret.logoUrl = h5UserInfo.headimgurl;

            return ret;
        }

        fun fromMiniProgramUserInfo(miniProgramUserData: WxMiniProgramUserData): WxUserData {
            val ret = WxUserData()
            ret.access_token = miniProgramUserData.access_token;
            ret.openid = miniProgramUserData.openid
            ret.nickName = miniProgramUserData.nickName;
            if (miniProgramUserData.gender == "1") {
                ret.sex = UserSexEnum.Male;
            } else if (miniProgramUserData.gender == "2") {
                ret.sex = UserSexEnum.Female;
            }
            ret.city = miniProgramUserData.city;
            ret.province = miniProgramUserData.province
            ret.country = miniProgramUserData.country;
            ret.logoUrl = miniProgramUserData.avatarUrl;
            return ret;
        }
    }
}


object wx {

    val appId get() = config.wxAppId
    val mchId get() = config.wxMchId


    val h5 = WxH5Group
    val officeAccount = WxOfficeAccountGroup
    val miniProgram = WxMiniProgramGroup
    val pay = WxPayGroup
    val sys = WxSystemGroup
}

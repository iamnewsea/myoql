package nbcp.wx

import nbcp.wx.h5.WxH5Group
import nbcp.wx.miniprogram.WxMiniProgramGroup
import nbcp.wx.officeaccount.WxOfficeAccountGroup
import nbcp.wx.pay.WxPayGroup
import nbcp.wx.system.WxSystemGroup

object wx {
    val h5 = WxH5Group
    val officeAccount = WxOfficeAccountGroup
    val miniProgram = WxMiniProgramGroup
    val pay = WxPayGroup
    val sys = WxSystemGroup
}
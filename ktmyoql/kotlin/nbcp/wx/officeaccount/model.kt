package nbcp.wx.officeaccount

data class wx_access_token(
        var appId: String = "",
        var token: String = "",
        var expires_in: Int = 7200
)
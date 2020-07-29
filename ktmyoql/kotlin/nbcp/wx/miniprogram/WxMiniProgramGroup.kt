package nbcp.wx.miniprogram

import nbcp.comm.*
import nbcp.db.redis.proxy.RedisStringProxy
import nbcp.utils.HttpUtil
import nbcp.wx.wx

object WxMiniProgramGroup {
    private val jscode2session = RedisStringProxy("jscode2session", 300)

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

    /**
     * https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/login/auth.code2Session.htm
     */
    fun getOpenId(appSecret: String, code: String): ApiResult<String> {
        require(appSecret.HasValue) { "缺少appSecret!" }

        var openId = jscode2session.get(code)
        if (openId.HasValue) {
            return ApiResult.of(openId);
        }

        val url = "https://api.weixin.qq.com/sns/jscode2session?appid=${wx.appId}&secret=${appSecret}&js_code=${code}&grant_type=authorization_code"

        val ajax = HttpUtil(url);
        var data = ajax.doGet().FromJson<WxLoginInfoModel>();
        if (data == null) {
            return ApiResult("网络错误")
        }

        if (data.errmsg.HasValue) {
            return ApiResult(data.errmsg)
        }

        openId = data.openid;
        jscode2session.set(code, openId);
        return ApiResult.of(openId);
    }


}
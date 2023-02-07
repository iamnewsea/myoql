package nbcp.myoql.weixin.miniprogram

import nbcp.base.comm.ApiResult
import nbcp.base.extend.FromJson
import nbcp.base.extend.HasValue
import nbcp.base.utils.HttpUtil
import nbcp.myoql.db.redis.proxy.RedisStringProxy
import nbcp.myoql.weixin.wx

object WxMiniProgramGroup {
    private fun jscode2session(key:String) = RedisStringProxy("wx:jscode2session:${key}", 300)

    /**
     * https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/login/auth.code2Session.html
     * wx.login 返回 code ,通过上述URL返回的数据
     */
    data class WxLoginInfoModel @JvmOverloads constructor(
        var openid: String = "",
        var session_key: String = "",
        var unionid: String = "",
        var errcode: String = "",
        var errmsg: String = ""
    )

    /**
     * https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/login/auth.code2Session.htm
     */
    @JvmStatic
    fun getOpenId(appSecret: String, code: String): nbcp.base.comm.ApiResult<String> {
        require(appSecret.HasValue) { "缺少appSecret!" }

        var openId = jscode2session(code).get()
        if (openId.HasValue) {
            return ApiResult.of(openId);
        }

        val url =
            "https://api.weixin.qq.com/sns/jscode2session?appid=${wx.appId}&secret=${appSecret}&js_code=${code}&grant_type=authorization_code"

        val ajax = HttpUtil(url);
        var data = ajax.doGet().FromJson<WxLoginInfoModel>();
        if (ajax.isError) {
            return ApiResult.error("接口调用出错", ajax.status);
        }

        if (data == null) {
            return ApiResult.error("接口调用没有返回数据!")
        }

        if (data.errmsg.HasValue) {
            return ApiResult.error(data.errmsg)
        }

        openId = data.openid;
        jscode2session(code).set(openId);
        return ApiResult.of(openId);
    }


}
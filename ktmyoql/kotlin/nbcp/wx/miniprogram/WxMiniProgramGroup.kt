package nbcp.wx.miniprogram

import nbcp.comm.*
import nbcp.db.redis.proxy.RedisStringProxy
import nbcp.utils.HttpUtil

object WxMiniProgramGroup {
    private val jscode2session = RedisStringProxy("jscode2session", 300)

    /**
     * https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/login/auth.code2Session.htm
     */
    fun getSessionCode(AppId: String, AppSecret: String, code: String): ApiResult<WxLoginInfoModel> {
        var data = jscode2session.get(code).FromJson<WxLoginInfoModel>()
        if (data != null) {
            return ApiResult.of(data);
        }

        val url = "https://api.weixin.qq.com/sns/jscode2session?appid=${AppId}&secret=${AppSecret}&js_code=${code}&grant_type=authorization_code"

        val ajax = HttpUtil(url);
        data = ajax.doGet().FromJson<WxLoginInfoModel>();
        if (data == null) {
            return ApiResult("网络错误")
        }

        if (data.errmsg.HasValue) {
            return ApiResult(data.errmsg)
        }

        jscode2session.set(code, data.ToJson());
        return ApiResult.of(data)
    }



}
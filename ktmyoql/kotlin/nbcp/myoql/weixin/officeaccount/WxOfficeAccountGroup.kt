package nbcp.myoql.weixin.officeaccount

import nbcp.base.comm.StringMap
import nbcp.base.extend.*
import nbcp.base.extend.*
import nbcp.base.extend.*
import nbcp.base.utils.HttpUtil
import nbcp.myoql.db.redis.proxy.RedisStringProxy
import nbcp.myoql.weixin.wx

object WxOfficeAccountGroup {
    private fun access_token(key: String) = RedisStringProxy("wx.access-token:${key}", 300)

    private fun jsapi_ticket(key: String) = RedisStringProxy("wx:jsapi-ticket:${key}", 300)

    /**
     * https://developers.weixin.qq.com/doc/offiaccount/WeChat_Invoice/Auto-print/API_Documentation.html#6.3%20%E8%8E%B7%E5%8F%96api_ticket
     */
    @JvmStatic
    fun getJsapiTicket(appSecret: String): nbcp.base.comm.ApiResult<String> {
        require(appSecret.HasValue) { "缺少appSecret!" }

        var tokenResult = getAccessToken(appSecret);

        var ret = nbcp.base.comm.ApiResult<String>();
        ret.msg = tokenResult.msg
        if (ret.msg.isNotEmpty()) return ret;

        var token = tokenResult.data!!

        var url = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=${token}&type=jsapi"
        val ajax = HttpUtil(url);
        val res = ajax.doGet().FromJson<StringMap>() ?: StringMap();
        if (ajax.isError) {
            ret.code = ajax.status;
            ret.msg = "接口调用出错!"
            return ret;
        }

        if (res.get("errcode").AsInt() != 0) {
            ret.msg = res["errmsg"].AsString();
            if (ret.msg.HasValue) {
                return ret;
            }
        }

        ret.data = res["ticket"].AsString()

        jsapi_ticket(wx.appId).set(ret.data!!)
        return ret;
    }

    data class wx_access_token @JvmOverloads constructor(
        var token: String = "",
        var expires_in: Int = 7200
    )

    /**
     * https://developers.weixin.qq.com/doc/offiaccount/Basic_Information/Get_access_token.html
     */
    @JvmStatic
    fun getAccessToken(appSecret: String): nbcp.base.comm.ApiResult<wx_access_token> {
        require(appSecret.HasValue) { "缺少appSecret!" }

        var token = access_token(wx.appId).get().FromJson<wx_access_token>()
        var ret = nbcp.base.comm.ApiResult<wx_access_token>();
        ret.data = token
        if (token != null) {
            return ret
        }

        var url =
            "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=${wx.appId}&secret=${appSecret}"
        val ajax = HttpUtil(url);
        val res = ajax.doGet().FromJson<StringMap>();
        if (ajax.isError) {
            ret.code = ajax.status;
            ret.msg = "接口调用出错!"
            return ret;
        }
        if (res == null) {
            ret.msg = "网络错误";
            return ret;
        }

        ret.msg = res["errmsg"].AsString();
        if (ret.msg.HasValue) {
            return ret;
        }
        ret.data = wx_access_token(res["access_token"].AsString(), res["expires_in"].AsInt())

        access_token(wx.appId).set(ret.data!!.ToJson())
        return ret;
    }
}
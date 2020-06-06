package nbcp.wx.officeaccount

import nbcp.comm.*
import nbcp.db.redis.proxy.RedisStringProxy
import nbcp.utils.HttpUtil

object WxOfficeAccountGroup {
    private val access_token = RedisStringProxy("office-account.access-token", 300)


    private val jsapi_ticket = RedisStringProxy("office-account.jsapi-ticket", 300)
    /**
     * https://developers.weixin.qq.com/doc/offiaccount/WeChat_Invoice/Auto-print/API_Documentation.html#6.3%20%E8%8E%B7%E5%8F%96api_ticket
     */
    fun getJsapiTicket(appId: String, appSecret: String): ApiResult<String> {
        var tokenResult = getAccessToken(appId,appSecret);

        var ret = ApiResult<String>();
        ret.msg = tokenResult.msg
        if( ret.msg.isNotEmpty()) return ret;

        var token = tokenResult.data!!

        var url = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=${token}&type=jsapi"
        val ajax = HttpUtil(url);
        val res = ajax.doGet().FromJson<StringMap>() ?: StringMap();

        if( res.get("errcode").AsInt() != 0) {
            ret.msg = res["errmsg"].AsString();
            if (ret.msg.HasValue) {
                return ret;
            }
        }

        ret.data =  res["ticket"].AsString()

        jsapi_ticket.set(appId, ret.data!!)
        return ret;
    }


    /**
     * https://developers.weixin.qq.com/doc/offiaccount/Basic_Information/Get_access_token.html
     */
    fun getAccessToken(appId: String, appSecret: String): ApiResult<wx_access_token> {
        var token = access_token.get(appId).FromJson<wx_access_token>()
        var ret = ApiResult<wx_access_token>();
        ret.data = token
        if (token != null) {
            return ret
        }

        var url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=${appId}&secret=${appSecret}"
        val ajax = HttpUtil(url);
        val res = ajax.doGet().FromJson<StringMap>();
        if (res == null) {
            ret.msg = "网络错误";
            return ret;
        }

        ret.msg = res["errmsg"].AsString();
        if (ret.msg.HasValue) {
            return ret;
        }
        ret.data = wx_access_token(appId, res["access_token"].AsString(), res["expires_in"].AsInt())

        access_token.set(appId, ret.data!!.ToJson())
        return ret;
    }
}
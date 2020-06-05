package nbcp.db.redis

import nbcp.comm.*
import nbcp.db.redis.proxy.RedisStringProxy
import nbcp.utils.HttpUtil
import nbcp.wx.*;

object WxGroup {
    private val access_token = RedisStringProxy("access_token", 300)
    private val jsapi_ticket = RedisStringProxy("jsapi_ticket", 300)

    /**
     * https://developers.weixin.qq.com/doc/offiaccount/Basic_Information/Get_access_token.html
     */
    fun getOfficialAccountAccessToken(appId: String, appSecret: String): ApiResult<wx_access_token> {
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


    private val jscode2session = RedisStringProxy("jscode2session", 300)

    fun getLoginResultFromLoginCode(AppId: String, AppSecret: String, code: String): ApiResult<WxLoginInfoModel> {
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


    fun getJsapiTicket(appId: String, appSecret: String): ApiResult<String> {
        var tokenResult = getOfficialAccountAccessToken(appId,appSecret);

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



}
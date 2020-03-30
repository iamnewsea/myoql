package nbcp.db.redis

import nbcp.base.extend.*
import nbcp.base.utils.HttpUtil
import nbcp.comm.ApiResult
import nbcp.comm.StringMap
import nbcp.db.redis.proxy.*
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.math.exp

class RedisBaseGroup {
    val validateCode = RedisStringProxy("validateCode", 180);

    open class WxGroup {
        private val access_token = RedisStringProxy("access_token", 300)

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
    }

    class CacheGroup {
        val cacheSqlData = RedisStringProxy("")
        fun brokeKeys(table: String) = RedisSetProxy("broke-keys:${table}")
        val borkeKeysChangedVersion = RedisNumberProxy("borke-keys-changed-version")
        val brokingTable = RedisStringProxy("broking-table")
    }

    val cache = CacheGroup()

    val wx = WxGroup()
}
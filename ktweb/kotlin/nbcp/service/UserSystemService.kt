package nbcp.service

import nbcp.comm.*
import nbcp.db.LoginUserModel
import nbcp.db.redis.proxy.RedisStringProxy
import nbcp.utils.SpringUtil
import nbcp.web.HttpContext
import nbcp.web.findParameterStringValue
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * 表示 config.userSystem 配置的用户体系的 redis 项，格式如： {config.userSystem}token:{id}
 */
@Component
class UserSystemService {
    @Value("\${app.user-system-header:user-system}")
    var userSystemHeader: String = "";

    /**
     * 用户体系：一般分为： admin,corp,open, 用于 redis key = {userSystem}token:{token}
     * 从三个地方获取 userSystem:
     * 1. 上下文 app.user-system
     * 2. request 参数 user-system
     * 3. 系统配置 app.user-system
     */
    private val userSystem: String
        get() {
            var value = scopes.getLatestStringScope("app.user-system")
            if (value.HasValue) {
                return value;
            }


            value = HttpContext.request.findParameterStringValue(userSystemHeader)
            if (value.HasValue) {
                return value;
            }

            value = SpringUtil.context.environment.getProperty("app.user-system") ?: ""
            if (value.HasValue) {
                return value;
            }
            throw RuntimeException("必须指定 app.user-system")
        }

    private val userSystemRedis
        get() = RedisStringProxy(userSystem + "token", 900);

    /**
     * 用户体系的redis验证码，格式如：{config.userSystem}validateCode:{id}
     */
    val validateCode get() = RedisStringProxy(userSystem + "validateCode", 180);


    /**
     * 获取登录token
     */
    fun getLoginInfoFromToken(token: String): LoginUserModel? {
        userSystemRedis.renewalKey(token);
        return userSystemRedis.get(token).FromJson<LoginUserModel>();
    }

    /**
     * 设置登录token,格式：{config.userSystem}token:{id}
     */
    fun saveLoginUserInfo(userInfo: LoginUserModel) {
        if (userInfo.id.HasValue && userInfo.token.HasValue) {
            userSystemRedis.set(userInfo.token, userInfo.ToJson())
        }
    }

    /**
     * 删除tokens
     */
    fun deleteToken(vararg tokens: String) {
        userSystemRedis.deleteKeys(*tokens)
    }
}
package nbcp.service

import nbcp.comm.*
import nbcp.db.LoginUserModel
import nbcp.db.redis.proxy.RedisStringProxy
import nbcp.utils.TokenUtil
import org.springframework.stereotype.Component

/**
 * 表示 config.userSystem 配置的用户体系的 redis 项，格式如： {config.userSystem}token:{id}
 */
@Component
class UserSystemService {

    private val userSystemRedisProxy
        get() = RedisStringProxy(config.tokenKey, config.tokenCacheSeconds)

    /**
     * 用户体系的redis验证码，格式如：{app.user-system}validateCode:{id}
     */
    val validateCode
        get() = RedisStringProxy(
            "validateCode", config.validateCodeCacheSeconds
        )


    /**
     * 获取登录token
     */
    fun getLoginInfoFromToken(token: String): LoginUserModel? {
        userSystemRedisProxy.renewalKey(token);
        return userSystemRedisProxy.get(token).FromJson<LoginUserModel>();
    }

    /**
     * 设置登录token,格式：{config.userSystem}token:{id}
     */
    fun saveLoginUserInfo(userInfo: LoginUserModel) {
        if (userInfo.id.HasValue && userInfo.token.HasValue) {
            userSystemRedisProxy.set(userInfo.token, userInfo.ToJson())
        }
    }

    /**
     * 删除tokens
     */
    fun deleteToken(vararg tokens: String) {
        userSystemRedisProxy.deleteKeys(*tokens)
    }
}
package nbcp.base.service

import nbcp.comm.*
import nbcp.db.LoginUserModel
import nbcp.db.redis.proxy.RedisStringProxy
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.stereotype.Component

interface IUserAuthenticationService {

    /**
     * 获取登录token
     */
    fun getLoginInfoFromToken(token: String, renewal: Boolean = false): LoginUserModel?

    /**
     * 设置登录token,格式：{config.userSystem}token:{id}
     */
    fun saveLoginUserInfo(userInfo: LoginUserModel)

    /**
     * 删除tokens
     */
    fun deleteToken(vararg tokens: String)
}

/**
 * 表示 config.userSystem 配置的用户体系的 redis 项，格式如： {config.userSystem}token:{id}
 */
@Component
@ConditionalOnMissingBean(IUserAuthenticationService::class)
class DefaultUserAuthenticationService : IUserAuthenticationService {

    /**
     * 保存到 Redis 的 token
     */
    private val userSystemRedis
        get() = RedisStringProxy(config.tokenKey, config.tokenCacheSeconds)

    /**
     * 用户体系的图片验证码，格式如：validateCode:{id}
     */
    val validateCodeRedis
        get() = RedisStringProxy(
            "validateCode", config.validateCodeCacheSeconds
        )


    /**
     * 获取登录token
     */
    override fun getLoginInfoFromToken(token: String, renewal: Boolean): LoginUserModel? {
        if (renewal) {
            userSystemRedis.renewalKey(token);
        }
        return userSystemRedis.get(token).FromJson<LoginUserModel>();
    }

    /**
     * 设置登录token,格式：{config.userSystem}token:{id}
     */
    override fun saveLoginUserInfo(userInfo: LoginUserModel) {
        if (userInfo.id.HasValue && userInfo.token.HasValue) {
            userSystemRedis.set(userInfo.token, userInfo.ToJson())
        }
    }

    /**
     * 删除tokens
     */
    override fun deleteToken(vararg tokens: String) {
        userSystemRedis.deleteKeys(*tokens)
    }
}
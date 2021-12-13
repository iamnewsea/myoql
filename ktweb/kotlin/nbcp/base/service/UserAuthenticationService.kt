package nbcp.base.service

import nbcp.comm.*
import nbcp.db.LoginUserModel
import nbcp.db.redis.proxy.RedisStringProxy
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.StringRedisTemplate
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

    fun getValidateCode(token: String): String

    fun setValidateCode(token: String, value: String)
}

/**
 * 表示 config.userSystem 配置的用户体系的 redis 项，格式如： {config.userSystem}token:{id}
 */
@Configuration
@ConditionalOnClass(StringRedisTemplate::class)
@ConditionalOnMissingBean(IUserAuthenticationService::class)
class DefaultUserAuthenticationService {
    class MyUserAuthService : IUserAuthenticationService {

        /**
         * 保存到 Redis 的 token
         */
        private val userSystemRedis
            get() = RedisStringProxy(config.tokenKey, config.tokenCacheSeconds)

        /**
         * 用户体系的图片验证码，格式如：validateCode:{id}
         */
        private val validateCodeRedis
            get() = RedisStringProxy(
                    "validateCode", config.validateCodeCacheSeconds
            )

        override fun getValidateCode(token: String): String {
            return validateCodeRedis.get(token);
        }

        override fun setValidateCode(token: String, value: String) {
            validateCodeRedis.set(token, value);
        }


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

    @Bean
    fun userAuthService(): MyUserAuthService {
        return MyUserAuthService();
    }
}


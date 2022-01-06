package nbcp.base.service

import nbcp.comm.*
import nbcp.db.LoginUserModel
import nbcp.db.redis.proxy.RedisStringProxy
import nbcp.web.tokenValue
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest

interface IUserAuthenticationService {

    /**
     * 获取登录token
     */
    fun getLoginInfoFromToken(request: HttpServletRequest, renewal: Boolean = false): LoginUserModel?

    /**
     * 设置登录token,格式：{config.userSystem}token:{id}
     * @return 返回缓存秒数
     */
    fun saveLoginUserInfo(request: HttpServletRequest, userInfo: LoginUserModel): Int

    /**
     * 删除tokens
     */
    fun deleteToken(request: HttpServletRequest)

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
        override fun getLoginInfoFromToken(request: HttpServletRequest, renewal: Boolean): LoginUserModel? {
            var token = request.tokenValue;
            if (token.isEmpty()) return null;

            if (renewal) {
                userSystemRedis.renewalKey(token);
            }
            return userSystemRedis.get(token).FromJson<LoginUserModel>();
        }

        /**
         * 设置登录token,格式：{config.userSystem}token:{id}
         */
        override fun saveLoginUserInfo(request: HttpServletRequest, userInfo: LoginUserModel): Int {
            if (userInfo.id.HasValue && userInfo.token.HasValue) {
                userSystemRedis.set(userInfo.token, userInfo.ToJson())
                return userSystemRedis.defaultCacheSeconds;
            }
            return 0
        }

        /**
         * 删除tokens
         */
        override fun deleteToken(request: HttpServletRequest) {
            var token = request.tokenValue;
            if (token.isEmpty()) return;
            userSystemRedis.deleteKeys(token)
        }
    }

    @Bean
    fun userAuthService(): MyUserAuthService {
        return MyUserAuthService();
    }
}


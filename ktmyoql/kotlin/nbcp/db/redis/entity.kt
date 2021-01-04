package nbcp.db.redis

import nbcp.comm.*
import nbcp.db.LoginUserModel
import nbcp.db.db
import nbcp.db.redis.proxy.*

/**
 * 缓存
 * 如果封装方法，会有数据库操作
 */
class RedisBaseGroup {
    class SqlCacheGroup {
        val cacheSqlData get() = RedisStringProxy("")
        fun brokeKeys(table: String) = RedisSetProxy("broke-keys:${table}")
        val borkeKeysChangedVersion get() = RedisNumberProxy("borke-keys-changed-version")
        val brokingTable get() = RedisStringProxy("broking-table")
    }

    //城市数据，缓存两个小时
    val cityCodeName get() = RedisStringProxy("city-code-name", 7200)

    /**
     * 获取城市
     */
    fun getCityNameByCode(code: Int): String {
        var name = cityCodeName.get(code.toString())
        if (name.HasValue) {
            return name;
        }


        //如果是 mongo, 如果是 mysql

        name = db.mor_base.sysCity.queryByCode(code)
            .select { it.name }
            .toEntity(String::class.java) ?: "";

        if (name.HasValue) {
            cityCodeName.set(code.toString(), name);
        }

        return name;
    }

    val sqlCache = SqlCacheGroup()

    /**
     * 表示 config.userSystem 配置的用户体系的 redis 项，格式如： {config.userSystem}token:{id}
     */
    val userSystem = UserSystemGroup()

    class UserSystemGroup {
        private val userSystemRedis
            get() = RedisStringProxy(config.userSystem + "token", 900);

        /**
         * 用户体系的redis验证码，格式如：{config.userSystem}validateCode:{id}
         */
        val validateCode get() = RedisStringProxy(config.userSystem + "validateCode", 180);


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
}
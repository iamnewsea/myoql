# redis

定义一个SystemEnum，表示有几个端的系统。
```
enum class SystemEnum {
    admin,
    corp,
    shop;

    companion object {
        lateinit var current: SystemEnum;
    }

    private val loginUserRedis
        get() = RedisStringProxy(this.toString() + "token");


    fun getLoginInfoFromRedisToken(token: String): LoginUserModel? {
        loginUserRedis.renewalKey(token);
        return loginUserRedis.get(token).FromJson<LoginUserModel>();
    }

    fun saveLoginUserInfoToRedis(token: String, userInfo: LoginUserModel) {
        loginUserRedis.set(token, userInfo.ToJson())
    }

    fun deleteToken(vararg tokens:String){
        loginUserRedis.deleteKeys(*tokens)
    }
}
```
使用 loginUserRedis.renewalKey 方法，让RedisKey在每次访问之后使用缓存自动续期。


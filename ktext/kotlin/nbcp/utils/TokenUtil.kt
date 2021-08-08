package nbcp.utils

import nbcp.comm.HasValue
import nbcp.comm.config
import java.lang.RuntimeException
import java.time.LocalDateTime

object TokenUtil {

//    /**requestToken
//     * 用户体系：一般分为： admin,corp,open, 用于 redis key = {userSystem}token:{token}
//     * 从两个地方获取 userSystem:
//     * 1. request 参数 token中  (st!{user-system}!{token值}
//     * 3. 系统配置 app.user-system
//     */
//    private fun getUserSystemType(requestToken: String = ""): String {
//        var value = requestToken
//        if (value.HasValue) {
//            var sects = value.split("!");
//            if (sects.size > 1) {
//                return sects[1];
//            }
//        }
//
//        value = config.getConfig("app.user-system", "")
//        if (value.HasValue) {
//            return value;
//        }
//
//        return ""
//    }


    /**
     * token前缀,SnowFlakeToken, 加上了一个 encodeURIComponent 不会转义的字符： - _ . ! ~ * ' ( )
     */
    private val tokenPrefix = "st!";

    /**
     * 根据用户体系生成新的 token。
     */
    @JvmOverloads
    fun generateToken(): String {
//        var userType = getUserSystemType(oldRequestToken)
//        if (userType.isEmpty()) {
//            throw RuntimeException("找不到用户体系的值")
//        }
        return tokenPrefix + CodeUtil.getCode();
    }

    fun getTokenCreateTime(token: String): LocalDateTime? {
        if (token.startsWith(tokenPrefix) == false) return null;

        try {
            return CodeUtil.getDateTimeFromCode(token.split("!")[2]);
        } catch (e: Exception) {
            return null;
        }
    }
}
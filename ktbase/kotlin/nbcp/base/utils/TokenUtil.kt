package nbcp.base.utils

import java.time.LocalDateTime

object TokenUtil {

    /**
     * token前缀,SnowFlakeToken, 加上了一个 encodeURIComponent 不会转义的字符： - _ . ! ~ * ' ( )
     */
    private val tokenPrefix = "st!";

    /**
     * 根据用户体系生成新的 token。
     */
    @JvmStatic
    fun generateToken(): String {
        return tokenPrefix + CodeUtil.getCode();
    }

    @JvmStatic
    fun getTokenCreateTime(token: String): LocalDateTime? {
        if (token.startsWith(tokenPrefix) == false) return null;

        try {
            return CodeUtil.getDateTimeFromCode(token.split("!")[1]);
        } catch (e: Exception) {
            return null;
        }
    }
}
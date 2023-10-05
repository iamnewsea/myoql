package nbcp.base.utils

import java.time.LocalDateTime

object TokenUtil {

    /**
     * 根据用户体系生成新的 token。
     */
    @JvmStatic
    fun generateToken(): String {
        return  CodeUtil.getCode();
    }

    @JvmStatic
    fun getTokenCreateTime(token: String): LocalDateTime? {
        try {
            return CodeUtil.getDateTimeFromCode(token);
        } catch (e: Exception) {
            return null;
        }
    }
}
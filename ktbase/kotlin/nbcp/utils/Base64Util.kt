package nbcp.utils

import nbcp.comm.const
import java.util.*

object Base64Util {
    /**
     * Url 中的安全字符  ()_.-*~
     */
    fun getUrlSafeValue(value: String): String {
        return value
            .replace('+', '-')
            .replace('/', '*')
            .replace('=', '~')
    }

    fun fromUrlSafeValue(value: String): String {
        return value
            .replace('-', '+')
            .replace('*', '/')
            .replace('~', '=')
    }

    /**
     * 获取 Base64
     */
    fun toBase64(target: String): String {
        return Base64.getEncoder().encodeToString(target.toByteArray(const.utf8));
    }

    fun toBase64(bytes: ByteArray): String {
        return Base64.getEncoder().encodeToString(bytes);
    }

    fun readFromBase64(base64: String): ByteArray {
        return Base64.getDecoder().decode(base64);
    }

    fun getStringContentFromBase64(base64: String): String {
        return String(readFromBase64(base64), const.utf8)
    }

}
package nbcp.utils

import nbcp.comm.utf8
import java.util.*
import javax.crypto.*
import javax.crypto.spec.*

/**
 * 加密解密工具
 */
object CipherUtil {
    /**
     * 返回 3des key base64格式的文本
     */
    fun get3desKey(): String {
        var encoder = Base64.getEncoder();
        return encoder.encodeToString(Des3Util.generateKey())
    }

    /**
     * 加密
     */
    fun encrypt3des(text: String, key: String): String {
        var encoder = Base64.getEncoder();
        var decoder = Base64.getDecoder();
        return encoder.encodeToString(Des3Util.encrypt(text.toByteArray(), decoder.decode(key)))
    }

    /**
     * 解密
     */
    fun decrypt3des(text: String, key: String): String {
        var decoder = Base64.getDecoder();
        return String(Des3Util.decrypt(decoder.decode(text), decoder.decode(key)))
    }

    /**
     * 3des
     * https://www.jianshu.com/p/3df4b2a12b3c
     */
    object Des3Util {
        private const val CIPHER_ALGORITHM = "DES/ECB/PKCS5Padding"
        private const val KEY_ALGORITHM = "DES"

        /**
         * 产生符合要求的Key,如果不用KeyGenerator随机性不好,而且要求自己对算法比较熟悉,能产生符合要求的Key
         */
        fun generateKey(): ByteArray {
            val kg: KeyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM)
            // 3DES要求使用112或者168位密钥
            // kg.init(112);
            kg.init(56)
            val secretKey: SecretKey = kg.generateKey()
            return secretKey.encoded
        }

        /**
         * 获取算法需要的安全密钥
         */
        private fun getSecretKey(key: ByteArray): SecretKey {
            val keySpec = DESKeySpec(key)
            // 3DES使用的密钥
            // DESedeKeySpec keySpec = new DESedeKeySpec(key);
            val kf = SecretKeyFactory.getInstance(KEY_ALGORITHM)
            return kf.generateSecret(keySpec)
        }

        /**
         * 加密数据
         */
        fun encrypt(text: ByteArray, key: ByteArray): ByteArray {
            val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(key))
            return cipher.doFinal(text)
        }

        /**
         * 解密数据
         */
        fun decrypt(text: ByteArray, key: ByteArray): ByteArray {
            val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(key))
            return cipher.doFinal(text)
        }
    }
}
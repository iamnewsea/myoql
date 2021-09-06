package nbcp.utils

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
        return MyUtil.getBase64(Des3Util.generateKey())
    }

    /**
     * 加密
     */
    fun encrypt3des(text: String, key: String): String {
        return MyUtil.getBase64(Des3Util.encrypt(text.toByteArray(), MyUtil.getFromBase64(key)))
    }

    /**
     * 解密
     */
    fun decrypt3des(text: String, key: String): String {
        return String(Des3Util.decrypt(MyUtil.getFromBase64(text), MyUtil.getFromBase64(key)))
    }


    /**
     * 返回 3des key base64格式的文本
     */
    fun getDesKey(): String {
        return MyUtil.getBase64(DesUtil.generateKey())
    }

    /**
     * 加密
     * @param key: 使用 get3desKey 生成的 key
     */
    fun encryptDes(text: String, key: String): String {
        return MyUtil.getBase64(DesUtil.encrypt(text.toByteArray(), MyUtil.getFromBase64(key)))
    }

    /**
     * 解密
     * @param key: 使用 get3desKey 生成的 key
     */
    fun decryptDes(text: String, key: String): String {
        return String(DesUtil.decrypt(MyUtil.getFromBase64(text), MyUtil.getFromBase64(key)))
    }

    /**
     * 3des
     * https://www.jianshu.com/p/3df4b2a12b3c
     */
    object DesUtil {
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

    object Des3Util {
        private const val CIPHER_ALGORITHM = "DESede/ECB/PKCS5Padding"
        private const val KEY_ALGORITHM = "DESede"

        /**
         * 产生符合要求的Key,如果不用KeyGenerator随机性不好,而且要求自己对算法比较熟悉,能产生符合要求的Key
         */
        fun generateKey(): ByteArray {
            val kg: KeyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM)
            // 3DES要求使用112或者168位密钥
            // kg.init(112);
            kg.init(112)
            val secretKey: SecretKey = kg.generateKey()
            return secretKey.encoded
        }

        /**
         * 获取算法需要的安全密钥
         */
        private fun getSecretKey(key: ByteArray): SecretKey {
            // 3DES使用的密钥
            var keySpec = DESedeKeySpec(key);
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
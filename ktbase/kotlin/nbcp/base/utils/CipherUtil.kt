package nbcp.base.utils

import nbcp.base.comm.const
import nbcp.base.extend.toUtf8String
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec
import javax.crypto.spec.DESedeKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and

/**
 * 加密解密工具
 */
object CipherUtil {
    /**
     * 返回 3des key base64格式的文本
     */
    @JvmStatic
    fun get3desKey(): String {
        return Des3Util.generateKey()
    }

    /**
     * 加密
     */
    @JvmStatic
    fun encrypt3des(text: String, key: String): String {
        return Base64Util.encode2Base64(Des3Util.encrypt(text.toByteArray(), Base64Util.decodeBase64(key)))
    }

    /**
     * 解密
     */
    @JvmStatic
    fun decrypt3des(text: String, key: String): String {
        return String(Des3Util.decrypt(Base64Util.decodeBase64(text), Base64Util.decodeBase64(key)))
    }

    /**
     *
     */
    @JvmStatic
    fun sha1(text: String): String {
        var digest = MessageDigest.getInstance("SHA1");
        digest.update(text.toByteArray(const.utf8));

        return digest.digest().map {
            (it.toInt() and 255).toString(16).padStart(2, '0')
        }.joinToString("").lowercase();
    }


    object AESUtil {
        private const val CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding"
        private const val KEY_ALGORITHM = "AES"

        @JvmStatic
        fun generateKey(): String {
            val kg: KeyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM)

            val secretKey: SecretKey = kg.generateKey()
            return secretKey.encoded.toUtf8String()
        }

        // 加密
        @JvmStatic
        fun encrypt(sSrc: String, sKey: String): String {
            val raw = sKey.toByteArray(const.utf8)
            val skeySpec = SecretKeySpec(raw, KEY_ALGORITHM)
            val cipher = Cipher.getInstance(CIPHER_ALGORITHM)//"算法/模式/补码方式"
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec)
            val encrypted = cipher.doFinal(sSrc.toByteArray(const.utf8))

            return  Base64Util.encode2Base64(encrypted)
        }

        // 解密
        @Throws(Exception::class)
        @JvmStatic
        fun decrypt(sSrc: String, sKey: String): String {
            val raw = sKey.toByteArray(const.utf8)
            val skeySpec = SecretKeySpec(raw, KEY_ALGORITHM)
            val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, skeySpec)
            val encrypted1 = Base64Util.decodeBase64(sSrc)//先用base64解密

            val original = cipher.doFinal(encrypted1)
            return String(original, const.utf8)
        }
    }


    /**
     * des
     * https://www.jianshu.com/p/3df4b2a12b3c
     */
    object DesUtil {
        private const val CIPHER_ALGORITHM = "DES/ECB/PKCS5Padding"
        private const val KEY_ALGORITHM = "DES"

        /**
         * 产生符合要求的Key,如果不用KeyGenerator随机性不好,而且要求自己对算法比较熟悉,能产生符合要求的Key
         */
        @JvmStatic
        fun generateKey(): String {
            val kg: KeyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM)
            // 3DES要求使用112或者168位密钥
            // kg.init(112);
            kg.init(56)
            val secretKey: SecretKey = kg.generateKey()
            return secretKey.encoded.toUtf8String()
        }

        /**
         * 获取算法需要的安全密钥
         */
        private @JvmStatic
        fun getSecretKey(key: ByteArray): SecretKey {
            val keySpec = DESKeySpec(key)
            // 3DES使用的密钥
            // DESedeKeySpec keySpec = new DESedeKeySpec(key);
            val kf = SecretKeyFactory.getInstance(KEY_ALGORITHM)
            return kf.generateSecret(keySpec)
        }

        /**
         * 加密数据
         */
        @JvmStatic
        fun encrypt(text: ByteArray, key: ByteArray): ByteArray {
            val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(key))
            return cipher.doFinal(text)
        }

        /**
         * 解密数据
         */
        @JvmStatic
        fun decrypt(text: ByteArray, key: ByteArray): ByteArray {
            val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(key))
            return cipher.doFinal(text)
        }
    }

    /**
     * 3des
     * https://www.jianshu.com/p/3df4b2a12b3c
     */
    object Des3Util {
        private const val CIPHER_ALGORITHM = "DESede/ECB/PKCS5Padding"
        private const val KEY_ALGORITHM = "DESede"

        /**
         * 产生符合要求的Key,如果不用KeyGenerator随机性不好,而且要求自己对算法比较熟悉,能产生符合要求的Key
         */
        @JvmStatic
        fun generateKey(): String {
            val kg: KeyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM)
            // 3DES要求使用112或者168位密钥
            // kg.init(112);
            kg.init(112)
            val secretKey: SecretKey = kg.generateKey()
            return secretKey.encoded.toUtf8String()
        }

        /**
         * 获取算法需要的安全密钥
         */
        private @JvmStatic
        fun getSecretKey(key: ByteArray): SecretKey {
            // 3DES使用的密钥
            var keySpec = DESedeKeySpec(key);
            val kf = SecretKeyFactory.getInstance(KEY_ALGORITHM)
            return kf.generateSecret(keySpec)
        }

        /**
         * 加密数据
         */
        @JvmStatic
        fun encrypt(text: ByteArray, key: ByteArray): ByteArray {
            val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(key))
            return cipher.doFinal(text)
        }

        /**
         * 解密数据
         */
        @JvmStatic
        fun decrypt(text: ByteArray, key: ByteArray): ByteArray {
            val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(key))
            return cipher.doFinal(text)
        }
    }
}
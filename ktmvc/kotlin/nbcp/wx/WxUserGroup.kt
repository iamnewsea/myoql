package nbcp.wx

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64
import nbcp.comm.utf8
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.AlgorithmParameters
import java.security.Security
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object WxUserGroup{
    /**
     * https://www.cnblogs.com/handsomejunhong/p/8670367.html
     * 解密用户的加密数据
     */
    fun decryptData(encryptedData: String, sessionKey: String, iv: String): String { // 被加密的数据
        val dataByte = Base64.decode(encryptedData)
        // 加密秘钥
        var keyByte = Base64.decode(sessionKey)
        // 偏移量
        val ivByte = Base64.decode(iv)
        // 如果密钥不足16位，那么就补足.  这个if 中的内容很重要
        val base = 16
        if (keyByte.size % base != 0) {
            val groups = keyByte.size / base + if (keyByte.size % base != 0) 1 else 0
            val temp = ByteArray(groups * base)
            Arrays.fill(temp, 0.toByte())
            System.arraycopy(keyByte, 0, temp, 0, keyByte.size)
            keyByte = temp
        }
        // 初始化
        Security.addProvider(BouncyCastleProvider())
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC")
        val spec = SecretKeySpec(keyByte, "AES")
        val parameters = AlgorithmParameters.getInstance("AES")
        parameters.init(IvParameterSpec(ivByte))
        cipher.init(Cipher.DECRYPT_MODE, spec, parameters) // 初始化
        val resultByte = cipher.doFinal(dataByte)
        if (null == resultByte || resultByte.size <= 0) {
            return "";
        }
        return String(resultByte, utf8)
    }
}
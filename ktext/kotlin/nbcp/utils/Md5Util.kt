package nbcp.utils

import org.apache.commons.codec.digest.DigestUtils
import java.math.BigInteger
import java.io.FileInputStream
import java.security.MessageDigest
import java.util.*


/**
 * Created by udi on 17-4-12.
 */
object Md5Util {
    fun getBase64Md5(value: String): String {
        return getBase64Md5(value.toByteArray())
    }

    /**
     * 带有校验和, 返回 base64 格式的 md5
     */
    fun getBase64Md5(source: ByteArray): String {
        if (source.size == 0) return ""
        val result = DigestUtils.md5(source).toMutableList();

        //再加两个 byte 做 checksum  % 65536  , Int16=Short
        var checksum = getChksum(source).toInt()

        result.add((checksum ushr 8).toByte())
        result.add((checksum % 255).toByte())

        return Base64.getEncoder().encodeToString(result.toByteArray())
                .replace('+', '!')
                .replace('/', '_')
    }

    /**
     * 计算校验和
     */
    fun getChksum(fileStream: FileInputStream): UShort {
        var buffer = ByteArray(1024);
        var len = 0;
        var checksum = 0;
        while (true) {
            len = fileStream.read(buffer, 0, 1024);
            if (len <= 0) break;

            for (i in buffer) {
                checksum += i

                if (checksum > 65535) {
                    checksum = checksum % 65535
                }
            }
        }

        return checksum.toUShort();
    }

    fun getChksum(source: ByteArray): UShort {
        var checksum = 0;
        for (i in source) {
            checksum += i

            if (checksum > 65535) {
                checksum = checksum % 65535
            }
        }
        return checksum.toUShort();
    }

    /**
     * 优先使用 getBase64Md5
     */
    fun getMd5(source: String): String {
        return DigestUtils.md5Hex(source)
    }

    fun getMd5(source: ByteArray): String {
        if (source.size == 0) return ""
        return DigestUtils.md5Hex(source)
    }

    fun getFileMD5(fileStream: FileInputStream): String {
        return DigestUtils.md5Hex(fileStream)
    }

    fun getFileBase64MD5(fileStream: FileInputStream): String {
        val result = DigestUtils.md5(fileStream).toMutableList();

        //再加两个 byte 做 checksum  % 65536  , Int16=Short
        var checksum = getChksum(fileStream).toInt();

        result.add((checksum ushr 8).toByte())
        result.add((checksum % 255).toByte())

        return Base64.getEncoder().encodeToString(result.toByteArray())
                .replace('+', '!')
                .replace('/', '_')
    }
}

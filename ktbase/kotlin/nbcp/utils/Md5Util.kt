package nbcp.utils

import org.apache.commons.codec.digest.DigestUtils
import java.io.File
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

        return Base64Util.getUrlSafeValue( MyUtil.getBase64(result.toByteArray()) )
    }

    /**
     * 计算校验和
     */
    fun getChksum(fileStream: FileInputStream): UShort {
        val buffer = ByteArray(1024);

        var checksum = 0;
        while (true) {
            val len = fileStream.read(buffer, 0, 1024);
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
     * 建议使用 getBase64Md5
     */
    fun getMd5(source: String): String {
        return DigestUtils.md5Hex(source)
    }

    fun getMd5(source: ByteArray): String {
        if (source.size == 0) return ""
        return DigestUtils.md5Hex(source)
    }

    fun getFileMD5(file: File): String {
        FileInputStream(file).use { fileStream -> return DigestUtils.md5Hex(fileStream) }
    }

    /**
     * 计算文件的 md5 + chksum ，返回 base64格式
     */
    fun getFileBase64MD5(file: File): String {

        var result = mutableListOf<Byte>()
        var checksum = 0;

        FileInputStream(file).use { fileStream ->
            result = DigestUtils.md5(fileStream).toMutableList();

            //再加两个 byte 做 checksum  % 65536  , Int16=Short
            checksum = getChksum(fileStream).toInt();
        }

        result.add((checksum ushr 8).toByte())
        result.add((checksum % 255).toByte())

        return  Base64Util.getUrlSafeValue( MyUtil.getBase64(result.toByteArray()))
    }
}

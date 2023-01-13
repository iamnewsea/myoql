package nbcp.base.utils

import nbcp.base.comm.*
import nbcp.base.data.ZipCompressData
import nbcp.base.extend.*
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.exception.ZipException
import net.lingala.zip4j.model.*
import net.lingala.zip4j.model.enums.*
import java.io.File
import java.nio.charset.Charset


object ZipUtil {

    @JvmStatic
    fun beginCompress(target: File): ZipCompressData {
        return ZipCompressData(target)
    }

    @JvmStatic
    fun deCompress(zipFile: String, charset: Charset, destDir: String, passwd: String): String {
        return deCompress(File(zipFile), charset, File(destDir), passwd);
    }

    /**
     * 根据所给密码解压zip压缩包到指定目录
     * 如果指定目录不存在,可以自动创建,不合法的路径将导致异常被抛出
     *
     * @param zipFile zip压缩包绝对路径
     * @param charset windows下压缩软件做出来的zip大多数是 GBK或GB2312, linux下压缩出的zip是 UTF8
     * @param destDir 指定解压文件夹位置
     * @param passwd 密码(可为空)
     * @return 解压后的文件数组
     * @throws ZipException
     */
    @JvmStatic
    fun deCompress(zipFile: File, charset: Charset, destDir: File, passwd: String): String {
        //1.判断指定目录是否存在
        if (!destDir.exists()) {
            if (destDir.mkdirs() == false) {
                return "创建文件夹 ${destDir.FullName} 失败"
            }
        }
        //2.初始化zip工具
        val zFile = ZipFile(zipFile)
        zFile.charset = charset
        if (!zFile.isValidZipFile()) {
            return "压缩文件不合法,可能被损坏."
        }
        //3.判断是否已加密
        if (zFile.isEncrypted()) {
            zFile.setPassword(passwd.toCharArray())
        }
        //4.解压所有文件
        zFile.extractAll(destDir.FullName)
        return "";
    }

    /**
     * 获取zipFile的内容
     */
    @JvmStatic
    fun listFile(zipFile: File, passwd: String): List<FileHeader> {
        val zFile = ZipFile(zipFile)
        zFile.charset = const.utf8
        if (!zFile.isValidZipFile()) {
            return listOf()
        }
        if (zFile.isEncrypted()) {
            zFile.setPassword(passwd.toCharArray())
        }
        return zFile.fileHeaders;
    }

}
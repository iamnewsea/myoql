package nbcp.utils

import nbcp.comm.FullName
import nbcp.comm.HasValue
import nbcp.comm.utf8
import net.lingala.zip4j.ZipFile
import java.io.File;

import org.springframework.util.StringUtils;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.*;
import net.lingala.zip4j.model.enums.*;
import java.lang.RuntimeException


object ZipUtil {
    fun compress(destFileName: String, passwd: String, vararg files: String):String{
        return compress(destFileName,passwd,*files.map { File(it) }.toTypedArray())
    }
    /**
     * 根据给定密码压缩文件(s)到指定目录
     *
     * @param destFileName 压缩文件存放绝对路径 e.g.:D:/upload/zip/demo.zip
     * @param passwd 密码(可为空)
     * @param files 单个文件或文件数组
     * @return 最终的压缩文件存放的绝对路径,如果为null则说明压缩失败.
     */
    fun compress(destFileName: String, passwd: String, vararg files: File): String {
        val parameters = ZipParameters()
        parameters.compressionMethod = CompressionMethod.DEFLATE // 压缩方式
        parameters.compressionLevel = CompressionLevel.NORMAL // 压缩级别
        if (passwd.HasValue) {
            parameters.isEncryptFiles = true
            parameters.encryptionMethod = EncryptionMethod.ZIP_STANDARD // 加密方式
        }

        val zipFile = ZipFile(destFileName)
        if (passwd.HasValue) {
            zipFile.setPassword(passwd.toCharArray())
        }
        addFiles(zipFile, parameters, *files);
        return ""
    }

    private fun addFiles(zipFile: ZipFile, parameters: ZipParameters, vararg files: File) {
        for (file in files) {
            if (file.isFile) {
                zipFile.addFile(file, parameters)
            } else {
                addFiles(zipFile, parameters, *file.listFiles());
            }
        }
    }

    fun deCompress(zipFile: String, destDir: String, passwd: String):String{
        return deCompress(File(zipFile),File(destDir),passwd);
    }

    /**
     * 根据所给密码解压zip压缩包到指定目录
     * 如果指定目录不存在,可以自动创建,不合法的路径将导致异常被抛出
     *
     * @param zipFile zip压缩包绝对路径
     * @param dest 指定解压文件夹位置
     * @param passwd 密码(可为空)
     * @return 解压后的文件数组
     * @throws ZipException
     */
    fun deCompress(zipFile: File, destDir: File, passwd: String):String  {
        //1.判断指定目录是否存在
        if (!destDir.exists()) {
            destDir.mkdir()
        }
        //2.初始化zip工具
        val zFile = ZipFile(zipFile)
        zFile.charset = utf8
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
//        val headerList = zFile.getFileHeaders()
//        val extractedFileList = mutableListOf<File>()
//        for (fileHeader in headerList) {
//            if (!fileHeader.isDirectory) {
//                extractedFileList.add(File(destDir, fileHeader.fileName))
//            }
//        }
//
//        return extractedFileList.toTypedArray();
    }

//    @JvmStatic
//    fun main(args: Array<String>) {
//        var folder = "D:\\test23\\";
//        compress("d:\\测试.zip", "123456", folder);
//        deCompress("d:\\测试.zip", "D:/test24", "123456")
//    }
}
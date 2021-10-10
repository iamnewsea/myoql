package nbcp.utils

import nbcp.comm.FullName
import nbcp.comm.HasValue
import nbcp.comm.const
import net.lingala.zip4j.ZipFile
import java.io.File;

import org.springframework.util.StringUtils;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.*;
import net.lingala.zip4j.model.enums.*;
import java.io.InputStream
import java.lang.RuntimeException

class ZipCompressData(file: File) {
    private var password = ""
    private val zipFile = ZipFile(file)

    fun withPassword(password: String): ZipCompressData {
        this.password = password;


        return this;
    }

    private fun getParam(): ZipParameters {
        val parameters = ZipParameters()

        parameters.compressionMethod = CompressionMethod.DEFLATE // 压缩方式
        parameters.compressionLevel = CompressionLevel.NORMAL // 压缩级别
        parameters.isOverrideExistingFilesInZip = true

        if (password.HasValue) {
            zipFile.setPassword(password.toCharArray())

            parameters.isEncryptFiles = true
            parameters.encryptionMethod = EncryptionMethod.ZIP_STANDARD // 加密方式
        } else {
            parameters.isEncryptFiles = false;
            parameters.encryptionMethod = EncryptionMethod.NONE
        }
        return parameters;
    }


    /**
     * 递归添加 file 下的子文件夹及子文件，并保留目录结构
     * @param file: 把file下的子文件夹及子文件进行添加。
     *
     */
    fun addAllSubFiles(file: File): ZipCompressData {
        if (file.isFile) {
            throw RuntimeException("不能添加文件,${file.FullName}")
        }

        addSubFiles(file.FullName, file.listFiles())
        return this;
    }

    fun addSubFiles(baseFile: String, files: Array<File>) {
        files.forEach { file ->
            if (file.isFile) {
                var param = getParam();
                param.fileNameInZip = getFileNameInZip(baseFile, file.FullName);
                zipFile.addFile(file, param)
            } else {
                addSubFiles(baseFile, file.listFiles())
            }
        }
    }

    private fun getFileNameInZip(baseFile: String, fullName: String): String {
        if (fullName.startsWith(baseFile) == false) {
            throw RuntimeException("目录结构的基础路径错误,${baseFile}:${fullName}")
        }

        return fullName.substring(baseFile.length + (if (baseFile.endsWith("\\")) 0 else 1))
    }


    @JvmOverloads
    fun addFile(stream: InputStream, fileNameInZip: String, fileComment: String = ""): ZipCompressData {
        val param = getParam();
        param.fileComment = fileComment
        param.fileNameInZip = fileNameInZip
        param.isOverrideExistingFilesInZip = true;

        zipFile.addStream(stream, param)
        return this;
    }
}


object ZipUtil {
    fun beginCompress(target: File): ZipCompressData {
        return ZipCompressData(target)
    }

    fun deCompress(zipFile: String, destDir: String, passwd: String): String {
        return deCompress(File(zipFile), File(destDir), passwd);
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
    fun deCompress(zipFile: File, destDir: File, passwd: String): String {
        //1.判断指定目录是否存在
        if (!destDir.exists()) {
            destDir.mkdir()
        }
        //2.初始化zip工具
        val zFile = ZipFile(zipFile)
        zFile.charset = const.utf8
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
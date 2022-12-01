package nbcp.base.data

import nbcp.base.extend.FullName
import nbcp.base.extend.HasValue
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.CompressionLevel
import net.lingala.zip4j.model.enums.CompressionMethod
import net.lingala.zip4j.model.enums.EncryptionMethod
import java.io.File
import java.io.InputStream

class ZipCompressData(file: File) {
    private var password = ""
    private val zipFile = ZipFile(file)
    private var zipParameters: ZipParameters? = null;

    fun withPassword(password: String): ZipCompressData {
        this.password = password;

        zipParameters = getNewZipParameter();
        return this;
    }

    private fun getNewZipParameter(): ZipParameters {
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
    fun add(file: File): ZipCompressData {
        if (file.isFile) {
            this.zipFile.addFile(file, zipParameters);
            return this;
        }

        this.zipFile.addFolder(file, zipParameters)
        return this;
    }


    @JvmOverloads
    fun addFile(stream: InputStream, fileNameInZip: String, fileComment: String = ""): ZipCompressData {
        val param = getNewZipParameter();
        param.fileComment = fileComment
        param.fileNameInZip = fileNameInZip

        zipFile.addStream(stream, param)
        return this;
    }
}
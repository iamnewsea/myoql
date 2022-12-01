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
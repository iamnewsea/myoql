package nbcp.base.mvc.service.upload

import nbcp.comm.*
import nbcp.utils.MyUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@Service
class LocalUploadBaseService : ISaveFileService {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    /**
     * 上传到本地时使用该配置,最后不带 "/"
     */
    @Value("\${app.upload.local.host:}")
    var UPLOAD_LOCAL_HOST: String = ""

    /**
     * 上传到本地时使用该配置,最后不带 "/"
     */
    @Value("\${app.upload.local.path:}")
    var UPLOAD_LOCAL_PATH: String = ""

    fun check(): String {
        if (UPLOAD_LOCAL_HOST.isEmpty()) {
            return "app.upload.local.host 配置为空!"
        }
        if (UPLOAD_LOCAL_PATH.isEmpty()) {
            return "app.upload.local.path 配置为空!"
        }

        return "";
    }

    override fun save(fileStream: InputStream, group: String, fileData: UploadFileNameData): String {
        check().apply {
            if (this.HasValue) {
                return this;
            }
        }

        var fileNames = fileData.getTargetFileName()

        val targetFileName =
            MyUtil.joinFilePath(UPLOAD_LOCAL_PATH, group, fileNames.joinToString(File.separator))

        val targetFile = File(targetFileName);

        if (targetFile.parentFile.exists() == false) {
            if (targetFile.parentFile.mkdirs() == false) {
                throw java.lang.RuntimeException("创建文件夹失败： ${targetFile.parentFile.FullName}")
            }
        }

        FileOutputStream(targetFile).use {
            if (fileStream.copyTo(it) <= 0) {
                throw java.lang.RuntimeException("保存文件失败： ${targetFile.FullName}")
            }

            logger.Important("文件保存成功: ${targetFile.FullName}")
        }

        return MyUtil.joinUrl(UPLOAD_LOCAL_HOST, group, fileNames.joinToString("/"));
    }

    override fun delete(url: String): JsonResult {
        var index = url.indexOf("//");
        if (index < 0) return JsonResult.error("url非法")

        var sects = url.Slice(index + 2).split("/");
        var path = MyUtil.joinFilePath(UPLOAD_LOCAL_PATH, sects.Skip(1).joinToString(File.separator))

        File(path).delete()

        return JsonResult()
    }
}
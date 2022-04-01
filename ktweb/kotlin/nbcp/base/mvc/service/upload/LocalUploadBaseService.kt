package nbcp.base.mvc.service.upload

import nbcp.comm.FullName
import nbcp.comm.HasValue
import nbcp.comm.JsonResult
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@Service
class LocalUploadBaseService : ISaveFileService {
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

      fun check(): Boolean {
        return UPLOAD_LOCAL_HOST.HasValue && UPLOAD_LOCAL_PATH.HasValue
    }

    override fun save(fileStream: InputStream, group: String, fileData: UploadFileNameData): String {
        if (check() == false) {
            return "";
        }

        val targetFileName = UPLOAD_LOCAL_HOST + fileData.getTargetFileName(File.separatorChar)

        val targetFile = File(targetFileName);

        if (targetFile.parentFile.exists() == false) {
            if (targetFile.parentFile.mkdirs() == false) {
                throw java.lang.RuntimeException("创建文件夹失败： ${targetFile.parentFile.FullName}")
            }
        }

        FileOutputStream(targetFile).use {
            if (fileStream.copyTo(it) <= 0) {
                throw java.lang.RuntimeException("保存文件失败： ${targetFile.parentFile.FullName}")
            }
        }
        return targetFileName;
    }

    override fun delete(url: String): JsonResult {
        throw NotImplementedError("未实现!")
    }
}
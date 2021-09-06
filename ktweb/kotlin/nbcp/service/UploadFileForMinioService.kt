package nbcp.service

import io.minio.MinioClient
import nbcp.comm.FullName
import nbcp.comm.HasValue
import nbcp.utils.MyUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@Service
class UploadFileForMinioService {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    @Value("\${app.upload.minio.endpoint:}")
    var MINIO_ENDPOINT: String = ""

    @Value("\${app.upload.minio.key:}")
    var MINIO_ACCESSKEY: String = ""

    @Value("\${app.upload.minio.secret:}")
    var MINIO_SECRETKEY: String = ""


    fun check(): Boolean {
        return MINIO_ENDPOINT.HasValue && MINIO_ACCESSKEY.HasValue && MINIO_SECRETKEY.HasValue
    }

    fun upload(fileStream: InputStream, group: String, fileData: UploadFileNameData): String {
        if (check() == false) {
            return "";
        }


        if (group.isEmpty()) {
            throw java.lang.RuntimeException("minIO需要group值！")
        }

        lateinit var minioClient: MinioClient
        try {
            minioClient = MinioClient(MINIO_ENDPOINT, MINIO_ACCESSKEY, MINIO_SECRETKEY)
        } catch (e: Exception) {
            logger.error(e.message + " . endpoint: ${MINIO_ENDPOINT}")
            throw e;
        }

        // bucket 不存在，创建
        if (!minioClient.bucketExists(group)) {
            minioClient.makeBucket(group)
        }

        val fileName = fileData.getTargetFileName()
            .replace(File.separatorChar, '/')

        //类型
        val contentType = MyUtil.getMimeType(fileData.extName)
        //把文件放置MinIo桶(文件夹)

        fileStream.use {
            minioClient.putObject(group, fileName, it, contentType)
        }

        return minioClient.getObjectUrl(group, fileName)
    }
}
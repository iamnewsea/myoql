package nbcp.service

import io.minio.*
import nbcp.comm.FullName
import nbcp.comm.HasValue
import nbcp.utils.MyUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@Service
@ConditionalOnClass(MinioClient::class)
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

    @Value("\${app.upload.minio.region:}")
    var MINIO_REGION: String = ""

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
            minioClient =
                MinioClient.builder().endpoint(MINIO_ENDPOINT).credentials(MINIO_ACCESSKEY, MINIO_SECRETKEY).build()
        } catch (e: Exception) {
            logger.error(e.message + " . endpoint: ${MINIO_ENDPOINT}")
            throw e;
        }

        // bucket 不存在，创建
        if (!minioClient.bucketExists(
                BucketExistsArgs
                    .builder()
                    .bucket(group)
                    .apply {
                        if (MINIO_REGION.HasValue) {
                            this.region(MINIO_REGION)
                        }
                    }
                    .build()
            )
        ) {

            minioClient.makeBucket(
                MakeBucketArgs
                    .builder()
                    .bucket(group)
                    .apply {
                        if (MINIO_REGION.HasValue) {
                            this.region(MINIO_REGION)
                        }
                    }
                    .build()
            )
        }

        val fileName = fileData.getTargetFileName('/')

        //类型
        val contentType = MyUtil.getMimeType(fileData.extName)
        //把文件放置MinIo桶(文件夹)

        return fileStream.use {
            var size = it.available().toLong();
            var response = minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(group).contentType(contentType)
                    .stream(it, size, -1)
                    .`object`(fileName)
                    .apply {
                        if (MINIO_REGION.HasValue) {
                            this.region(MINIO_REGION)
                        }
                    }
                    .build()
            )


            //TODO @yuxh 应该返回可下载的URL地址，这里不对。
            return@use MINIO_ENDPOINT + "/" + response.bucket() + "/" + response.`object`()
        }
    }
}
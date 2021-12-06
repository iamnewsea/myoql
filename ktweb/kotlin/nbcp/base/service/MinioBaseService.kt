package nbcp.base.service

import io.minio.*
import nbcp.comm.FullName
import nbcp.comm.HasValue
import nbcp.comm.JsonResult
import nbcp.comm.Skip
import nbcp.utils.MyUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@Service
@ConditionalOnClass(MinioClient::class)
class MinioBaseService : InitializingBean {
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

    private val minioClient by lazy {
        return@lazy MinioClient.builder().endpoint(MINIO_ENDPOINT).credentials(MINIO_ACCESSKEY, MINIO_SECRETKEY).build()
    }

    fun upload(fileStream: InputStream, group: String, fileData: UploadFileNameData): String {
        if (check() == false) {
            throw java.lang.RuntimeException("minIO缺少配置项！")
        }

        if (group.isEmpty()) {
            throw java.lang.RuntimeException("minIO需要group值！")
        }

        //不自动创建桶。因为要提前创建，要对桶进行授权。

//        var exists = try {
//            minioClient.bucketExists(
//                BucketExistsArgs
//                    .builder()
//                    .bucket(group)
//                    .apply {
//                        if (MINIO_REGION.HasValue) {
//                            this.region(MINIO_REGION)
//                        }
//                    }
//                    .build()
//            )
//        } catch (e: Exception) {
//            logger.error(e.message + ". 可能连接错误导致获取桶出错!")
//            throw e;
//        }

        // bucket 不存在，创建
//        if (!exists) {
//
//            minioClient.makeBucket(
//                MakeBucketArgs
//                    .builder()
//                    .bucket(group)
//                    .apply {
//                        if (MINIO_REGION.HasValue) {
//                            this.region(MINIO_REGION)
//                        }
//                    }
//                    .build()
//            )
//        }

        val fileName = fileData.getTargetFileName('/')

        //类型
        val contentType = MyUtil.getMimeType(fileData.extName)
        //把文件放置MinIo桶(文件夹)

        return fileStream.use {
            val size = it.available().toLong();
            val response = minioClient.putObject(
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

            return@use listOf(MINIO_ENDPOINT, response.bucket(), response.`object`())
                .map { it.trim('/') }
                .joinToString("/")
        }
    }


    fun delete(url: String): JsonResult {
        var path = url;
        if (path.startsWith("http://", true) || path.startsWith("https://", true)) {
            if (path.startsWith(MINIO_ENDPOINT, true) == false) {
                return JsonResult.error("不识别Minio地址")
            }

            path = path.substring(MINIO_ENDPOINT.length);
        }

        var sects = path.split('/').filter { it.HasValue };
        var group = sects[0];
        var name = sects.Skip(1).joinToString("/")


        minioClient.removeObject(
            RemoveObjectArgs.builder()
                .bucket(group)
                .`object`(name)
                .apply {
                    if (MINIO_REGION.HasValue) {
                        this.region(MINIO_REGION)
                    }
                }
                .build()
        )

        return JsonResult();
    }

    override fun afterPropertiesSet() {

    }
}
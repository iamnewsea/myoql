package nbcp.web.service.upload

import io.minio.*
import nbcp.base.comm.JsonResult
import nbcp.base.comm.ListResult
import nbcp.base.extend.AsString
import nbcp.base.extend.HasValue
import nbcp.base.extend.Skip
import nbcp.base.utils.UrlUtil
import nbcp.base.utils.WebUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.stereotype.Service
import java.io.InputStream

@Service
@ConditionalOnClass(MinioClient::class)
class MinioBaseService : ISaveFileService {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    /**
     * 内部存储使用，一般是 http://ip:9000
     */
    @Value("\${app.upload.minio.api-host:}")
    var API_HOST: String = ""

    /**
     * 外部访问使用，一般是 https://域名:9000
     */
    @Value("\${app.upload.minio.web-host:}")
    lateinit var WEB_HOST: String

    /**
     * minio:9001 --> service account 创建。
     */
    @Value("\${app.upload.minio.key:}")
    lateinit var MINIO_ACCESSKEY: String

    /**
     * minio:9001 --> service account 创建。
     */
    @Value("\${app.upload.minio.secret:}")
    lateinit var MINIO_SECRETKEY: String

    @Value("\${app.upload.minio.region:}")
    lateinit var MINIO_REGION: String

    fun check(): Boolean {
        return (API_HOST.HasValue || WEB_HOST.HasValue) && MINIO_ACCESSKEY.HasValue && MINIO_SECRETKEY.HasValue
    }


    private val minioClient by lazy {
        var host = WebUtil.getFullHttpUrl(API_HOST.AsString(WEB_HOST))


        return@lazy MinioClient.builder().endpoint(host)
            .credentials(MINIO_ACCESSKEY, MINIO_SECRETKEY).build()
    }


    fun createBucket(group: String) {
        var ret = minioClient.makeBucket(
            MakeBucketArgs
                .builder()
                .bucket(group)
                .apply {
                    if (MINIO_REGION.HasValue) {
                        this.region(MINIO_REGION)
                    }
                }
                .build());


        //应该再通过Api 设置权限为 Public ，但是好像Api太复杂。
        //minioClient.getBucketPolicy()
//        minioClient.setBucketPolicy(
//            SetBucketPolicyArgs
//                .builder()
//                .bucket(group)
//                .config("""""")
//                .build()
//        )
    }


    /**
     * @param group: 桶，必须提前创建好,并设置桶为 Public。
     */
    override fun save(
        fileStream: InputStream,
        group: String,
        fileData: UploadFileNameData
    ): String {
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

        val fileName = fileData.getTargetFileName().joinToString("/")

        //类型
        val contentType = WebUtil.getMimeType(fileData.extName).AsString("application/octet-stream")
        //把文件放置MinIo桶(文件夹)

        return fileStream.use {
            val size = it.available().toLong();
            val response = minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(group)
                    .contentType(contentType)
                    .stream(it, size, -1)
                    .`object`(fileName)
                    .apply {
                        if (MINIO_REGION.HasValue) {
                            this.region(MINIO_REGION)
                        }
                    }
                    .build()
            )

            var responseHost = WebUtil.getHostUrlWithoutHttp(WEB_HOST.AsString(API_HOST))

            return@use UrlUtil.joinUrl(responseHost, response.bucket(), response.`object`())
        }
    }


    fun listFile(bucket: String, path: String): ListResult<String> {
        var list = minioClient.listObjects(
            ListObjectsArgs.builder()
                .bucket(bucket)
                .prefix(path)
                .build()
        )
            .filter { it.get().isDir == false }
            .map {
                it.get().objectName()
            };

        return ListResult.of(list);
    }

    public


    override fun delete(url: String): JsonResult {
        var path = WebUtil.getHostUrlWithoutHttp(url);
        var apiHost = WebUtil.getHostUrlWithoutHttp(API_HOST);
        var webHost = WebUtil.getHostUrlWithoutHttp(WEB_HOST);

        if (path.startsWith(apiHost, true)) {
            path = path.substring(apiHost.length);
        } else if (path.startsWith(webHost, true)) {
            path = path.substring(webHost.length);
        } else {
            return JsonResult.error("不识别Minio地址")
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
}
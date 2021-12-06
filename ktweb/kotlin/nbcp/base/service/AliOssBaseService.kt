package nbcp.base.service

import com.aliyun.oss.OSS
import com.aliyun.oss.OSSClientBuilder
import com.aliyun.oss.internal.OSSHeaders
import com.aliyun.oss.model.*
import io.minio.MinioClient
import nbcp.comm.FullName
import nbcp.comm.HasValue
import nbcp.comm.const.size1m
import nbcp.utils.MyUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.stereotype.Service
import org.springframework.util.MimeTypeUtils
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.lang.Exception
import java.net.URL
import java.util.*
import java.util.concurrent.*

@Service
@ConditionalOnClass(OSSClientBuilder::class)
class AliOssBaseService {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    @Value("\${app.upload.ali.host:}")
    var ALI_HOST: String = ""

    @Value("\${app.upload.ali.key:}")
    var ALI_ACCESSKEY: String = ""

    @Value("\${app.upload.ali.secret:}")
    var ALI_SECRETKEY: String = ""

    val ossClient by lazy {
        return@lazy OSSClientBuilder().build(ALI_HOST, ALI_ACCESSKEY, ALI_SECRETKEY)
    }

    fun check(): Boolean {
        return ALI_HOST.HasValue && ALI_ACCESSKEY.HasValue && ALI_SECRETKEY.HasValue
    }

    fun upload(fileStream: InputStream, group: String, fileData: UploadFileNameData): String {

        if (check() == false) {
            return "";
        }

        var contentLength = fileStream.available();


        //5、如果需要在初始化分片时设置文件存储类型，并通过setContentType决定链接是预览还是下载
        val metadata = ObjectMetadata()
        metadata.setContentType(MyUtil.getMimeType(fileData.extName))
        metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString())
        metadata.setContentLength(contentLength.toLong())

        val fileName = fileData.getTargetFileName('/')

        ossClient.putObject(group, fileName, fileStream, metadata);

        var endpoint = ALI_HOST;
        if (endpoint.startsWith("http://", true)) {
            endpoint = endpoint.substring("http://".length)
        } else if (endpoint.startsWith("https://", true)) {
            endpoint = endpoint.substring("https://".length)
        }
        return "https://${group}.${endpoint}${fileName}"
    }

}
package nbcp.web.base.mvc.service.upload

import com.aliyun.oss.OSSClientBuilder
import com.aliyun.oss.internal.OSSHeaders
import com.aliyun.oss.model.ObjectMetadata
import com.aliyun.oss.model.StorageClass
import nbcp.base.comm.*;
import nbcp.base.db.*;
import nbcp.base.enums.*;
import nbcp.base.extend.*;
import nbcp.base.utils.*;
import nbcp.base.utils.MyUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.stereotype.Service
import java.io.InputStream

@Service
@ConditionalOnClass(OSSClientBuilder::class)
class AliOssBaseService : ISaveFileService {
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

    override fun save(fileStream: InputStream, group: String, fileData: UploadFileNameData): String {

        if (check() == false) {
            return "";
        }

        var contentLength = fileStream.available();


        //5、如果需要在初始化分片时设置文件存储类型，并通过setContentType决定链接是预览还是下载
        val metadata = ObjectMetadata()
        metadata.setContentType(MyUtil.getMimeType(fileData.extName).AsString("application/octet-stream"))
        metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString())
        metadata.setContentLength(contentLength.toLong())

        val fileName = fileData.getTargetFileName().joinToString("/")

        ossClient.putObject(group, fileName, fileStream, metadata);

        var endpoint = ALI_HOST;
        if (endpoint.startsWith("http://", true)) {
            endpoint = endpoint.substring("http://".length)
        } else if (endpoint.startsWith("https://", true)) {
            endpoint = endpoint.substring("https://".length)
        }
        return "https://${group}.${endpoint}${fileName}"
    }

    override fun delete(url: String): JsonResult {
        throw NotImplementedError("未实现!")
    }
}
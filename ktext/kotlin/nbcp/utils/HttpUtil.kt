package nbcp.utils

/**
 * Created by udi on 17-4-30.
 */

import nbcp.comm.*
import org.apache.http.ssl.SSLContexts
import org.slf4j.LoggerFactory
import java.awt.image.BufferedImage
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
import java.io.IOException
import java.lang.RuntimeException
import java.security.KeyStore
import java.time.LocalDateTime
import java.util.*
import javax.imageio.ImageIO
import javax.net.ssl.SSLSocketFactory


data class FileMessage(
    var fullPath: String = "",
    var name: String = "",
    var extName: String = "",
    var msg: String = ""
);

/**
 * http://tools.jb51.net/table/http_content_type/
 */
fun getTextTypeFromContentType(contentType: String): Boolean {
    return contentType.contains("json", true) ||
            contentType.contains("htm", true) ||
            contentType.contains("text", true) ||
            contentType.contains("urlencoded", true)
}

data class HttpRequestData(
    var instanceFollowRedirects: Boolean = false,
    var useCaches: Boolean = false,
    var connectTimeout: Int = 5_000,
    var readTimeout: Int = 30_000,
    var chunkedStreamingMode: Int = 0,

    var requestMethod: String = "",
    var contentType: String = "",
    var headers: StringMap = StringMap()
) {
    init {
        headers.set("Connection", "close")
    }

    /**
     * postAction 是上传专用
     */
    var postAction: ((DataOutputStream) -> Unit)? = null

    /**
     * post 小数据量
     */
    var postBody = ""

    /**
     * 请求内容是否是文字
     */
    val postIsText: Boolean
        get() {
            return getTextTypeFromContentType(this.contentType)
        }
}

class HttpResponseData {

    /**
     * 回发的原始内容。处理回发文本
     */
    var resultBody: String = ""

    /**
     * 回发回调，处理下载大文件。
     */
    var resultAction: ((DataInputStream) -> Unit)? = null


    /**
     * 回发内容是否是文字
     */
    val resultIsText: Boolean
        get() {
            return getTextTypeFromContentType(this.contentType)
        }

    var contentType: String = ""
        internal set;

    /**
     * 该次回发Header，只读 ,全小写
     */
    var headers: StringMap = StringMap()
        internal set;

    /**
     * 回发的编码，只读
     */
    val charset: String
        get() {
            var char_parts = this.contentType.AsString().split(";").last().split("=");
            if (char_parts.size == 2) {
                if (char_parts[0].trim().VbSame("charset")) {
                    return char_parts[1];
                }
            }
            return "UTF-8"
        }
}
//@Configuration
//class RestTemplateConfig {
//    @Bean
//    fun restTemplate(factory: ClientHttpRequestFactory?): RestTemplate {
//        return RestTemplate(factory)
//    }
//
//    @Bean
//    fun simpleClientHttpRequestFactory(): ClientHttpRequestFactory {
//        val factory = SimpleClientHttpRequestFactory()
//        factory.setConnectTimeout(15000)
//        factory.setReadTimeout(5000)
//        return factory
//    }
//}

/*
 * 尽量使用 RestTemplate.
 * 封装了 HttpURLConnection 进行网络请求。
 */
class HttpUtil(var url: String = "") {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)

        /**远程下载图片,并压缩
         * @param imagePath :图片的目录,系统下载后, 会先尝试 remoteImage 名字进行保存,如果失败,则使用唯一Id进行保存.
         */
        @JvmStatic
        fun getImage(remoteImage: String, imagePath: String, maxWidth: Int = 1200): FileMessage {
            var imagePathFile = File(imagePath)
            if (imagePathFile.exists() == false) {
                imagePathFile.mkdirs();
            }

            var ret = FileMessage();
            var extInfo = FileExtentionInfo(remoteImage);
            if (extInfo.extName.isEmpty()) {
                extInfo.extName = "png";
                extInfo.extType = FileExtentionTypeEnum.Image;
            }

            ret.name = CodeUtil.getCode();
            ret.extName = extInfo.extName;

            var tempFile = imagePath + File.separatorChar + ret.name + "." + extInfo.extName;

            ret.fullPath = tempFile;

            var oriImage: BufferedImage
            try {
                oriImage = ImageIO.read(URL(remoteImage));
            } catch (e: Exception) {
                ret.msg = "读取图片错误：" + remoteImage + "." + e.Detail;
                return ret;
            }

            var height = oriImage.height;
            var width = oriImage.width;
            if (maxWidth > 0 && oriImage.width > maxWidth) {
                width = maxWidth;
                height = oriImage.height * width / oriImage.width
            }

            var destImageData = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            destImageData.getGraphics().drawImage(oriImage, 0, 0, width, height, null); // 绘制缩小后的图

            /*
    * JPEGImageEncoder 将图像缓冲数据编码为 JPEG 数据流。该接口的用户应在 Raster 或
    * BufferedImage 中提供图像数据，在 JPEGEncodeParams 对象中设置必要的参数， 并成功地打开
    * OutputStream（编码 JPEG 流的目的流）。JPEGImageEncoder 接口可 将图像数据编码为互换的缩略
    * JPEG 数据流，该数据流将写入提供给编码器的 OutputStream 中。
    * 注意：com.sun.image.codec.jpeg 包中的类并不属于核心 Java API。它们属于 Sun 发布的 JDK
    * 和 JRE 产品的组成部分。虽然其它获得许可方可能选择发布这些类，但开发人员不能寄 希望于从非 Sun
    * 实现的软件中得到它们。我们期望相同的功能最终可以在核心 API 或标准扩 展中得到。
    */
            var baos = FileOutputStream(tempFile);
            ImageIO.write(destImageData, "jpeg", baos);



            if (extInfo.name.length > 1) {
                var oriFile = imagePath + File.separatorChar + extInfo.name + "." + extInfo.extName;
                //判断是否存在原始文件
                if (File(oriFile).exists() == false) {
                    File(tempFile).renameTo(File(oriFile));

                    ret.fullPath = oriFile;
                }
            }

            return ret;
        }
    }

    var request = HttpRequestData()
    var response = HttpResponseData()


    /**
     * 加密使用，可用于 PKCS12
     */
    var sslSocketFactory: SSLSocketFactory? = null

    /**
     * 该次回发的状态码，只读
     */
    var status: Int = 0
        private set;

    /**
     * 请求耗时时间
     */
    var totalTime: TimeSpan = TimeSpan(0)
        private set;

    /**
     * 该次回发过程中的错误消息，只读
     */
    var msg: String = ""  //初始化失败的消息.用于对象传递
        private set;


    fun setPostBody(postBody: String): HttpUtil {
        this.request.postBody = postBody;
        return this
    }


    fun doGet(): String {
        this.request.requestMethod = "GET"

        var retData = doNet()

        return retData;
    }

    /**
     * Post请求
     */
    fun doPost(postJson: JsonMap): String {

        if (this.request.contentType.isEmpty()) {
            this.request.contentType = "application/json;charset=UTF-8"
        }

        var requestBody = "";
        if (this.request.contentType.contains("json")) {
            requestBody = postJson.ToJson()
        } else {
            requestBody =
                postJson.map { it.key + "=" + JsUtil.encodeURIComponent(it.value.AsString()) }.joinToString("&");
        }

        return doPost(requestBody);
    }

    /**
     * Post请求
     */
    fun doPost(requestBody: String = ""): String {
//        logger.Info { "[post]\t${url}\n${requestHeader.map { it.key + ":" + it.value }.joinToString("\n")}" }

        if (this.request.headers.containsKey("Accept") == false) {
            this.request.headers.set("Accept", "application/json")
        }

        this.request.requestMethod = "POST"

        if (requestBody.HasValue) {
            this.setPostBody(requestBody)
        }

        var ret = doNet()


        return ret;
    }

    fun doNet(): String {
        var startAt = LocalDateTime.now();

        var conn = URL(url).openConnection() as HttpURLConnection;

        try {
            if (this.sslSocketFactory != null) {
                (conn as javax.net.ssl.HttpsURLConnection)
                    .setSSLSocketFactory(this.sslSocketFactory)
            }

            conn.instanceFollowRedirects = this.request.instanceFollowRedirects
            conn.useCaches = this.request.useCaches
            conn.connectTimeout = this.request.connectTimeout
            conn.readTimeout = this.request.readTimeout

            conn.requestMethod = this.request.requestMethod
            if (this.request.chunkedStreamingMode > 0) {
                conn.setChunkedStreamingMode(this.request.chunkedStreamingMode)
            }

            if (this.request.contentType.HasValue &&
                (this.request.headers.containsKey("Content-Type") || this.request.headers.containsKey("ContentType"))
            ) {
                throw RuntimeException("请使用 contentType 属性")
            }

            if (this.request.contentType.HasValue) {
                conn.setRequestProperty("Content-Type", this.request.contentType);
            }

            this.request.headers.keys.forEach { key ->
                conn.setRequestProperty(key, this.request.headers.get(key))
            }


            if (conn.requestMethod.isNullOrEmpty()) {
                throw RuntimeException("没有设置 method！");
            }

            conn.doInput = true

            //https://bbs.csdn.net/topics/290053257
            //GET,HEAD,OPTIONS
            if (conn.requestMethod.toLowerCase().IsIn("post", "put")) {
                conn.doOutput = true


                //如果是 post 小数据
                if (this.request.postBody.any()) {
                    conn.setChunkedStreamingMode(0)

                    DataOutputStream(conn.outputStream).use { out ->
                        out.write(this.request.postBody.toByteArray(utf8));
                        out.flush();
                    }
                } else if (this.request.postAction != null) {
                    DataOutputStream(conn.outputStream).use { out ->
                        this.request.postAction?.invoke(out);
                        out.flush();
                    }
                }
            }

            this.status = conn.responseCode
            this.response.contentType = conn.contentType ?: ""

            conn.headerFields.forEach {
                if (it.key == null) {
                    return@forEach
                }
                var value = it.value.joinToString(",")
                this.response.headers[it.key.toLowerCase()] = value
            }

            var responseStream: InputStream? = null
            if (conn.responseCode.Between(200, 299)) {
                responseStream = conn.inputStream;
            } else {
                responseStream = conn.errorStream;
            }

            if (responseStream != null) {
                if (this.response.resultAction != null) {
                    DataInputStream(responseStream).use { input -> this.response.resultAction?.invoke(input) }
                } else if (this.response.resultIsText) {
                    DataInputStream(responseStream).use { input ->
                        this.response.resultBody =
                            toByteArray(input).toString(Charset.forName(this.response.charset));
                    }
                }
            }

            this.totalTime = LocalDateTime.now() - startAt
            return this.response.resultBody
        } finally {
            // 断开连接
            if (this.totalTime.totalMilliseconds == 0L) {
                this.totalTime = LocalDateTime.now() - startAt
            }


            logger.InfoError(this.status != 200) {
                var msgs = mutableListOf<String>();
                msgs.add("${conn!!.requestMethod} ${url}\t[status:${this.status}]");

                msgs.add(this.request.headers.map {
                    return@map "\t${it.key}:${it.value}"
                }.joinToString(line_break))

                if (this.status == 0) {
                    msgs.add("[Timeout]");
                } else {

                    var k10Size = 10240
                    //小于 10K
                    if (this.request.postIsText && this.request.postBody.any()) {
                        msgs.add("---")
                        msgs.add(this.request.postBody.take(k10Size).toByteArray().toString(utf8))
                    }

                    msgs.add("---")

                    msgs.add(this.response.headers.map {
                        return@map "\t${it.key}:${it.value}"
                    }.joinToString(line_break))

                    //小于10K
                    if (this.response.resultIsText && this.response.resultBody.any()) {
                        msgs.add(
                            this.response.resultBody.take(k10Size).toByteArray()
                                .toString(Charset.forName(this.response.charset.AsString("UTF-8")))
                        )
                    }
                }

                var content = msgs.joinToString(line_break);
                msgs.clear();
                return@InfoError content;
            }

            conn.disconnect();
        }
    }


    fun toByteArray(input: InputStream): ByteArray {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(4096)
        var n = 0
        while (true) {
            n = input.read(buffer);
            if (n == -1) {
                break;
            }
            output.write(buffer, 0, n)
        }
        return output.toByteArray()
    }

    /**
     * 下载文件
     * @param filePath:保存位置，优先使用Url中的文件名，如果存在，则用唯一Code命名。
     */
    fun doDownloadFile(filePath: String): FileMessage {
        var CACHESIZE = 1024 * 1024;

        var remoteImage = url;
        var ret = FileMessage();
        var extInfo = FileExtentionInfo(remoteImage);
        if (extInfo.extName.isEmpty()) {
            extInfo.extName = "png";
            extInfo.extType = FileExtentionTypeEnum.Image;
        }

        ret.name = extInfo.name;
        ret.extName = extInfo.extName;

        var oriFile = filePath + File.separatorChar + extInfo.name + "." + extInfo.extName;
        var tempFile = filePath + File.separatorChar + CodeUtil.getCode() + "." + extInfo.extName;

        ret.fullPath = tempFile;

        var destFilePath = File(tempFile)

        try {
            if (destFilePath.parentFile.exists() == false) {
                destFilePath.parentFile.mkdirs();
            }
        } catch (e: Exception) {
            ret.msg = e.message ?: "创建目标文件夹${destFilePath.parent}失败!";
            return ret;
        }

        if (destFilePath.createNewFile() == false) {
            ret.msg = "创建文件${tempFile} 失败"
            return ret;
        }

        this.request.requestMethod = "GET"

        this.response.resultAction = { input ->

            var bytes = ByteArray(CACHESIZE);
            var bytes_len = 0;

            while (true) {
                bytes_len = input.read(bytes)
                if (bytes_len <= 0) {
                    break;
                }

                destFilePath.appendBytes(bytes.sliceArray(0 until bytes_len))
            }
        }

        doNet();

        //判断是否存在原始文件
        if (File(oriFile).exists() == false) {
            File(tempFile).renameTo(File(oriFile));

            ret.fullPath = oriFile;
        }

        return ret;
    }


    /**
     * 大文件上传文件，块大小1MB
     * @param filePath: 要上传的文件。
     */
    fun uploadFile(filePath: String): String {
        var CACHESIZE = 1024 * 1024;

        var file = File(filePath);
        if (file.exists() == false) {
            throw  Exception("文件${filePath}不存在")
        }

        var fileName = file.name;

        var boundary = "------" + CodeUtil.getCode();

        this.request.requestMethod = "POST"
        this.request.connectTimeout = 1200_000
        this.request.readTimeout = 1200_000
        this.request.headers.set("Connection", "keep-alive")
        this.request.headers.set("Content-Type", "multipart/form-data; boundary=${boundary}")
        this.request.chunkedStreamingMode = CACHESIZE

//        var isTxt = false;
//        this.setResponse { conn ->
//            isTxt = getTextTypeFromContentType(conn.contentType)
//        }

        this.request.postAction = { out ->
            out.write(
                """--${boundary}
Content-Disposition: form-data; name="${fileName}"; filename="blob"
Content-Type: application/octet-stream

""".replace("\n", "\r\n").toByteArray()
            )


            var bytes = ByteArray(CACHESIZE);
            var bytes_len = 0;
            DataInputStream(FileInputStream(file)).use { input ->
                while (true) {
                    bytes_len = input.read(bytes)
                    if (bytes_len <= 0) {
                        break;
                    }

                    out.write(bytes, 0, bytes_len)
                }
            }

            out.write("\r\n--${boundary}--".toByteArray())
        }

        var ret = this.doNet()

        if (this.response.resultIsText) {
            logger.info(ret.Slice(0, 4096))
        }

        return ret;
    }
}
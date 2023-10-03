package nbcp.base.utils

/**
 * Created by udi on 17-4-30.
 */

import nbcp.base.comm.*
import nbcp.base.data.HttpRequestData
import nbcp.base.data.HttpResponseData
import nbcp.base.db.*
import nbcp.base.enums.FileExtensionTypeEnum
import nbcp.base.enums.HttpMethod
import nbcp.base.extend.*
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEvent
import java.awt.image.BufferedImage
import java.io.*
import java.net.HttpURLConnection
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.URL
import java.nio.charset.Charset
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.time.Duration
import java.time.LocalDateTime
import javax.imageio.ImageIO
import javax.net.ssl.*


data class FileMessage @JvmOverloads constructor(
        var fullPath: String = "",
        var name: String = "",
        var extName: String = "",
        var msg: String = ""
);


class HttpUtilPreEvent(val http:HttpUtil): ApplicationEvent(http) {

}


class HttpUtilPostEvent(val http:HttpUtil): ApplicationEvent(http) {

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
class HttpUtil @JvmOverloads constructor(url: String = "") {

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)

        /**
         * http://tools.jb51.net/table/http_content_type/
         */
        @JvmStatic
        fun getTextTypeFromContentType(contentType: String): Boolean {
            return contentType.contains("json", true) ||
                    contentType.contains("htm", true) ||
                    contentType.contains("text", true) ||
                    contentType.contains("xml", true) ||
                    contentType.contains("urlencoded", true)
        }


        @JvmStatic
        val localIpAddresses by lazy {
            val allNetInterfaces = NetworkInterface.getNetworkInterfaces()
            var ips = mutableListOf<String>()
            while (allNetInterfaces.hasMoreElements()) {
                val netInterface = allNetInterfaces.nextElement() as NetworkInterface
                if (netInterface.isLoopback || netInterface.isVirtual || !netInterface.isUp) {
                    continue
                }

                val addresses = netInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val ip = addresses.nextElement()
                    if (ip != null && ip is Inet4Address) {
                        ips.add(ip.getHostAddress())
                    }
                }
            }
            return@lazy ips.toSet();
        }

        /**
         * 使用 Basic $base64(用户名:密码) 格式
         */
        @JvmStatic
        fun getBasicAuthorization(userName: String, password: String): String {
            return "Basic " + Base64Util.encode2Base64("${userName}:${password}".toByteArray())
        }

        @JvmStatic
        fun getLoginNamePassword(basicAuthorization: String): LoginNamePasswordData {
            if (basicAuthorization.startsWith("Basic ")) return LoginNamePasswordData();
            var value = Base64Util.decodeBase64Utf8(basicAuthorization.substring("Basic ".length));
            var index = value.indexOf(":");
            if (index < 0) return LoginNamePasswordData();
            return LoginNamePasswordData(value.substring(0, index), value.substring(index + 1))
        }

        /**远程下载图片,并压缩
         */
        @JvmStatic
        @JvmOverloads
        fun getImage(remoteImage: String, maxWidth: Int = 1200): ByteArray {
            var oriImage: BufferedImage
            oriImage = ImageIO.read(URL(remoteImage));

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
            var baos = ByteArrayOutputStream()
            ImageIO.write(destImageData, "jpeg", baos);
            return baos.toByteArray();
        }
    }

    private var maxRetryTimes = 0;
    private var currentRetryTimes = 0;
    private var retrySleepSeconds: ((Int) -> Int) = { it * 3 } //重试时间隔一秒。
    private var retryEnabled = true;

    var url: String = ""
        get
        set(value) {
            field = WebUtil.getFullHttpUrl(value)
        }

    init {
        this.url = url
    }

    /**
     * 回发回调，处理下载大文件。
     */
    var resultAction: ((DataInputStream) -> Unit)? = null


    /**
     * retrySleepSeconds: 默认= maxRetryTimes * 3
     */
    fun withMaxTryTimes(maxRetryTimes: Int, retrySleepSeconds: ((Int) -> Int)? = null): HttpUtil {
        this.maxRetryTimes = maxRetryTimes;
        this.currentRetryTimes = 0;
        if (retrySleepSeconds != null) {
            this.retrySleepSeconds = retrySleepSeconds;
        }
        this.retryEnabled = true;
        return this;
    }


    var request = HttpRequestData()
    var response = HttpResponseData()

    var logResponseLength = 1024;

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
     * 是否有错误
     */
    val isError: Boolean
        get() {
            return this.isSuccess == false
        }

    val isSuccess: Boolean
        get() {
            return this.status.IfUntil(200, 400)
        }

    /**
     * 请求耗时时间
     */
    var totalTime: Duration = Duration.ZERO
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

    fun doGet(queryJson: StringMap): String {
        return doGet(queryJson.toUrlQuery())
    }

    @JvmOverloads
    fun doGet(query: String = ""): String {
        this.request.httpMethod = HttpMethod.GET

        if (query.HasValue) {
            var queryObj = UrlUtil.parseUrlQueryJson(if (query.startsWith('?')) query else "?" + query)
            var urlObj = UrlUtil.parseUrlQueryJson(this.url);
            urlObj.queryJson.putAll(queryObj.queryJson);
            this.url = urlObj.toUrl();
        }
        return doNet()
    }

    fun doPut(postJson: Map<String, Any?>): String {
        if (this.request.contentType.isEmpty()) {
            this.request.contentType = "application/json;charset=UTF-8"
        }

        var requestBody = "";
        if (this.request.contentType.contains("json", true)) {
            requestBody = postJson.ToJson()
        } else {
            requestBody =
                    postJson.map { it.key + "=" + UrlUtil.encodeURIComponent(it.value.AsString()) }.joinToString("&");
        }

        return doPut(requestBody);
    }

    /**
     * Post请求
     */
    fun doPost(postJson: Map<String, Any?>): String {
        if (this.request.contentType.isEmpty()) {
            this.request.contentType = "application/json;charset=UTF-8"
        }

        var requestBody = "";
        if (this.request.contentType.contains("json", true)) {
            requestBody = postJson.ToJson()
        } else {
            requestBody =
                    postJson.map { it.key + "=" + UrlUtil.encodeURIComponent(it.value.AsString()) }.joinToString("&");
        }

        return doPost(requestBody);
    }

    @JvmOverloads
    fun doPut(requestBody: String = ""): String {
//        logger.Info { "[post]\t${url}\n${requestHeader.map { it.key + ":" + it.value }.joinToString("\n")}" }

        if (this.request.headers.containsKeyIgnoreCase("Accept") == false) {
            this.request.headers.set("Accept", "application/json")
        }

        this.request.httpMethod = HttpMethod.PUT

        if (requestBody.HasValue) {
            this.setPostBody(requestBody)
        }

        return doNet()
    }

    /**
     * Post请求
     */
    @JvmOverloads
    fun doPost(requestBody: String = ""): String {
//        logger.Info { "[post]\t${url}\n${requestHeader.map { it.key + ":" + it.value }.joinToString("\n")}" }

        if (this.request.headers.containsKeyIgnoreCase("Accept") == false) {
            this.request.headers.set("Accept", "application/json")
        }

        this.request.httpMethod = HttpMethod.POST

        if (requestBody.HasValue) {
            this.setPostBody(requestBody)
        }

        return doNet()
    }


    /**
     * 信任所有服务器地址,不检查任何证书
     */
    fun trustAllHttpsHosts() {
        //创建SSLContext对象，并使用我们指定的信任管理器初始化
        val trustAllCert: TrustManager = object : X509TrustManager {
            //返回受信任的X509证书数组。
            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }

            //该方法检查客户端的证书,由于我们不需要对客户端进行认证，所以可以不用处理
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}

            //该方法检查服务器的证书，通过实现该方法，可以指定我们信任的任何证书。
            //不做任何处理，就会信任任何证书。
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
        }

        // 安装全信任管理器
        val sc: SSLContext = SSLContext.getInstance("TLS")
        sc.init(null, arrayOf(trustAllCert), SecureRandom())
        this.sslSocketFactory = sc.getSocketFactory();
//        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory())
    }

    private var error: Exception? = null
    private fun clearData() {
        this.status = 0
        this.response = HttpResponseData()
        this.totalTime = Duration.ZERO
        this.msg = ""
        this.error = null;
    }

    fun doNet(): String {
        clearData()

        SpringUtil.context.publishEvent(HttpUtilPreEvent(this))

        var startAt = LocalDateTime.now();

        var conn = URL(url).openConnection() as HttpURLConnection;

        try {
            if (conn is HttpsURLConnection) {
                if (this.sslSocketFactory == null) {
                    this.trustAllHttpsHosts();
                }

                conn.setSSLSocketFactory(this.sslSocketFactory)
                conn.hostnameVerifier = object : HostnameVerifier {
                    override fun verify(p0: String, p1: SSLSession): Boolean {
                        return true;
                    }
                }
            }

            conn.instanceFollowRedirects = this.request.instanceFollowRedirects
            conn.useCaches = this.request.useCaches
            conn.connectTimeout = this.request.connectTimeout
            conn.readTimeout = this.request.readTimeout

            conn.requestMethod = this.request.httpMethod.toString()
            if (this.request.chunkedStreamingMode > 0) {
                conn.setChunkedStreamingMode(this.request.chunkedStreamingMode)
            }


            this.request.headers.keys.forEach { key ->
                conn.setRequestProperty(key, this.request.headers.get(key))
            }


            if (conn.requestMethod.isNullOrEmpty()) {
                this.retryEnabled = false;
                throw RuntimeException("没有设置 method！");
            }

            conn.doInput = true

            //https://bbs.csdn.net/topics/290053257
            //GET,HEAD,OPTIONS
            if (conn.requestMethod.lowercase().IsIn("post", "put")) {
                conn.doOutput = true

                //调用 conn.outputStream ，会进行连接。

                //如果是 post 小数据
                if (this.request.postBody.any()) {
                    conn.setChunkedStreamingMode(0)

                    DataOutputStream(conn.outputStream).use { out ->
                        out.write(this.request.postBody.toByteArray(const.utf8));
                        out.flush();
                    }
                } else if (this.request.postAction != null) {
                    ByteArrayOutputStream().use { out ->
                        this.request.postAction?.invoke(out);
                        out.writeTo(conn.outputStream)
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
                this.response.headers[it.key.lowercase()] = value
            }

            var responseStream: InputStream?
            if (conn.responseCode.IfUntil(200, 400)) {
                responseStream = conn.inputStream;
            } else {
                responseStream = conn.errorStream;
            }

            SpringUtil.context.publishEvent(HttpUtilPostEvent(this))

            if (responseStream != null) {
                if (this.resultAction != null) {
                    DataInputStream(responseStream).use { input -> this.resultAction?.invoke(input) }
                } else if (this.response.resultIsText) {
                    DataInputStream(responseStream).use { input ->
                        this.response.resultBody =
                                toByteArray(input).toString(Charset.forName(this.response.charset));
                    }
                }
            }

            this.totalTime = LocalDateTime.now() - startAt

            logger.Info { this.getLogMessage() }
            return this.response.resultBody
        } catch (e: Exception) {
            this.error = e;
        } finally {
            // 断开连接
            if (this.totalTime.seconds == 0L) {
                this.totalTime = LocalDateTime.now() - startAt
            }

            conn.disconnect();
        }


        this.currentRetryTimes++;
        if (this.retryEnabled && this.status == 0 && this.currentRetryTimes < this.maxRetryTimes) {
            var sleep = this.retrySleepSeconds.invoke(this.currentRetryTimes)
            logger.Important("连接超时,${sleep} 秒后将进行第 ${this.currentRetryTimes} 次重试 ${this.url}")

            if (sleep > 0) {
                Thread.sleep(sleep.AsLong() * 1000)
            }

            return this.doNet();
        } else {
            logger.error(this.getLogMessage())
        }
        throw this.error!!;
    }

    private fun getLogMessage(): String {
        var msgs = mutableListOf<String>();
        msgs.add("${this.request.httpMethod} ${url}\t[status:${this.status}]");

        msgs.add(this.request.headers.map {
            return@map "\t${it.key}:${it.value}"
        }.joinToString(const.line_break))

        if (this.status == 0) {
            msgs.add("[Timeout]");

            if (this.maxRetryTimes > 0 && this.currentRetryTimes == this.maxRetryTimes) {
                msgs.add("[重试了 ${this.maxRetryTimes} 次，终是网络失败!]");
            }
        } else {
            var subLen = logResponseLength / 2;
            //小于 1K
            if (this.request.postIsText && this.request.postBody.any()) {
                msgs.add("---")
                if (this.request.postBody.length < logResponseLength) {
                    msgs.add(this.request.postBody)
                } else {
                    msgs.add(
                            this.request.postBody.substring(
                                    0,
                                    subLen
                            ) + "\n〘…〙\n" + this.request.postBody.Slice(-subLen)
                    )
                }
            }

            msgs.add("---")

            msgs.add(this.response.headers.map {
                return@map "\t${it.key}:${it.value}"
            }.joinToString(const.line_break))

            //小于1K
            if (this.response.resultIsText && this.response.resultBody.any()) {

                if (this.response.resultBody.length < logResponseLength) {
                    msgs.add(this.response.resultBody)
                } else {
                    msgs.add(
                            this.response.resultBody.substring(
                                    0,
                                    subLen
                            ) + "\n〘…〙\n" + this.response.resultBody.Slice(-subLen)
                    )
                }
            }
        }


        val content = msgs.joinToString(const.line_break);
        msgs.clear();
        return content;
    }

    fun toByteArray(input: InputStream): ByteArray {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(4096)

        while (true) {
            val n = input.read(buffer);
            if (n == -1) {
                break;
            }
            output.write(buffer, 0, n)
        }
        return output.toByteArray()
    }

    /**
     * 下载文件
     * @param filePath:保存位置，优先使用Url中的文件名，如果为空，则用唯一Code命名。
     */
    fun doDownloadFile(targetFileName: String): FileMessage {
        val CACHESIZE = 1024 * 1024;

        val remoteImage = url;
        val ret = FileMessage();
        val extInfo = FileExtensionInfo.ofUrl(remoteImage);
        if (extInfo.extName.isEmpty()) {
            extInfo.extName = "png";
            extInfo.extType = FileExtensionTypeEnum.IMAGE;
        }

        ret.name = extInfo.name;
        ret.extName = extInfo.extName;

        var fileName = targetFileName.AsString { UrlUtil.joinUrl("./", extInfo.getFileName()) }

        ret.fullPath = fileName;

        val destFilePath = File(fileName)

        try {
            if (destFilePath.parentFile.exists() == false) {
                destFilePath.parentFile.mkdirs();
            }
        } catch (e: Exception) {
            ret.msg = e.message ?: "创建目标文件夹${destFilePath.parent}失败!";
            return ret;
        }

        if (destFilePath.createNewFile() == false) {
            ret.msg = "创建文件${fileName} 失败"
            return ret;
        }

        this.request.httpMethod = HttpMethod.GET
        this.resultAction = { input ->
            val bytes = ByteArray(CACHESIZE);

            while (true) {
                val bytes_len = input.read(bytes)
                if (bytes_len <= 0) {
                    break;
                }

                destFilePath.appendBytes(bytes.sliceArray(0 until bytes_len))
            }
        }

        doNet();

        return ret;
    }

    private val CACHESIZE = 1024 * 1024;
    fun submitForm(form: Map<String, Any>): String {
        var items = form
                .filter { it.value::class.java.IsSimpleType() }
                .mapValues { it.value.AsString() }
                .filter { it.value.HasValue }

        var files = form
                .filter { it.value::class.java.IsSimpleType() == false }
                .mapValues {
                    var v = it.value;
                    if (v is UploadFileResource) {
                        return@mapValues v;
                    }

                    if (v is File) {
                        return@mapValues UploadFileResource(v.name, v.inputStream())
                    }

                    throw RuntimeException("不识别的类型:${v::class.java.name}")
                }

        return submitForm(items, files)
    }


    /**
     * 提交表单
     */
    fun submitForm(items: Map<String, String>, files: Map<String, UploadFileResource>): String {
        // https://blog.csdn.net/Sunfj0821/article/details/104605290/

        val boundary = CodeUtil.getCode();

        this.request.httpMethod = HttpMethod.POST
        this.request.headers.set("Connection", "keep-alive")
        this.request.headers.set("Content-Type", "multipart/form-data; boundary=${boundary}")
        this.request.chunkedStreamingMode = CACHESIZE

        this.request.postAction = { out ->
            writeFormSimple(out, items, boundary);
            writeFormFile(out, files, boundary);


            out.write("--${boundary}--".toByteArray(const.utf8))
        }

        val ret = this.doNet()

        if (this.response.resultIsText) {
            logger.info(ret.Slice(0, 4096))
        }

        return ret;
    }

    private fun writeFormSimple(out: ByteArrayOutputStream, map: Map<String, String>, boundary: String) {

        map.keys.forEach { fileName ->
            var value = map.get(fileName)!!;

            var contentLength = -1;


            if (this.request.headers.containsKeyIgnoreCase("Transfer-Encoding") == false) {
                contentLength = value.AsString().toByteArray(const.utf8).size;
            }

            out.writeLine("--${boundary}")
            writeDispositionHeaders(out, fileName, "text/plain;charset=UTF-8", contentLength)

            out.writeNewLine()
            out.writeLine(value.AsString());
        }

    }

    private fun writeFormFile(out: ByteArrayOutputStream, map: Map<String, UploadFileResource>, boundary: String) {

        map.keys.forEach { fileName ->
            var resource = map.get(fileName)!!;

            var contentLength = -1;


            if (this.request.headers.containsKeyIgnoreCase("Transfer-Encoding") == false) {
                contentLength = resource.stream.available();
            }


            out.writeLine("--${boundary}")
            writeDispositionHeaders(
                    out,
                    fileName,
                    "application/octet-stream",
                    contentLength,
                    resource.fileName.AsString("filename")
            )


            out.writeNewLine()
            resource.stream.copyTo(out, CACHESIZE)
            out.writeNewLine()
        }

    }

    // /home/udi/.m2/repository/org/springframework/spring-web/5.3.20/spring-web-5.3.20-sources.jar!/org/springframework/http/converter/FormHttpMessageConverter.java
//    private fun writeBoundary(out: OutputStream, boundary: String) {
//        out.writeLine("--${boundary}")
//    }

    private fun writeDispositionHeaders(
            out: OutputStream,
            key: String,
            contentType: String,
            contentLength: Int = 0,
            fileName: String = ""
    ) {
        if (fileName.isEmpty()) {
            out.writeLine("""Content-Disposition: form-data; name="${key}"""")
        } else {
            out.writeLine("""Content-Disposition: form-data; name="${key}"; filename="${fileName}"""")
        }

        out.writeLine("""Content-Type: ${contentType}""")


        if (contentLength > 0) {
            out.writeLine("""Content-Length: ${contentLength}""")
        }
    }

    private fun OutputStream.writeText(txt: String) {
        this.write("${txt}".toByteArray(const.utf8));
    }

    private fun OutputStream.writeLine(txt: String) {
        this.write("${txt}\r\n".toByteArray(const.utf8));
    }

    private fun OutputStream.writeNewLine() {
        this.write("\r\n".toByteArray());
    }


//    private fun writePart( name:String, HttpEntity<?> partEntity, OutputStream os) throws IOException {
//        Object partBody = partEntity.getBody();
//        if (partBody == null) {
//            throw new IllegalStateException("Empty body for part '" + name + "': " + partEntity);
//        }
//        Class<?> partType = partBody.getClass();
//        HttpHeaders partHeaders = partEntity.getHeaders();
//        MediaType partContentType = partHeaders.getContentType();
//        for (HttpMessageConverter<?> messageConverter : this.partConverters) {
//            if (messageConverter.canWrite(partType, partContentType)) {
//                Charset charset = isFilenameCharsetSet() ? StandardCharsets.US_ASCII : this.charset;
//                HttpOutputMessage multipartMessage = new MultipartHttpOutputMessage(os, charset);
//                multipartMessage.getHeaders().setContentDispositionFormData(name, getFilename(partBody));
//                if (!partHeaders.isEmpty()) {
//                    multipartMessage.getHeaders().putAll(partHeaders);
//                }
//                ((HttpMessageConverter<Object>) messageConverter).write(partBody, partContentType, multipartMessage);
//                return;
//            }
//        }
//        throw new HttpMessageNotWritableException("Could not write request: no suitable HttpMessageConverter " +
//                "found for request type [" + partType.getName() + "]");
//    }

    /**
     * 大文件上传文件，块大小1MB
     * @param fileName: 文件名。
     * @param fileStream: 文件输入流。
     * @return 上传文件返回的结果，如Json：{id,name,url,msg}
     */
//    fun upload1File(fileName: String, fileStream: InputStream): String {
//        val CACHESIZE = 1024 * 1024;
//
//        val boundary = "------" + CodeUtil.getCode();
//
//        this.request.requestMethod = "POST"
//        this.request.connectTimeout = 1200_000
//        this.request.readTimeout = 1200_000
//        this.request.headers.set("Connection", "keep-alive")
//        this.request.headers.set("Content-Type", "multipart/form-data; boundary=${boundary}")
//        this.request.chunkedStreamingMode = CACHESIZE
//
////        var isTxt = false;
////        this.setResponse { conn ->
////            isTxt = getTextTypeFromContentType(conn.contentType)
////        }
//
//        this.request.postAction = { out ->
//            out.write(
//                """--${boundary}
//Content-Disposition: form-data; name="${fileName}"; filename="${fileName}"
//Content-Type: application/octet-stream
//
//""".replace("\n", "\r\n").toByteArray()
//            )
//
//
//            val bytes = ByteArray(CACHESIZE);
//            fileStream.use { input ->
//                while (true) {
//                    val bytes_len = input.read(bytes)
//                    if (bytes_len <= 0) {
//                        break;
//                    }
//                    out.write(bytes, 0, bytes_len)
//                }
//            }
//
//            out.write("\r\n--${boundary}--".toByteArray())
//        }
//
//        val ret = this.doNet()
//
//        if (this.response.resultIsText) {
//            logger.info(ret.Slice(0, 4096))
//        }
//
//        return ret;
//    }
}
package nbcp.utils

/**
 * Created by udi on 17-4-30.
 */

import nbcp.comm.*
import nbcp.db.LoginNamePasswordData
import org.slf4j.LoggerFactory
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
class HttpUtil @JvmOverloads constructor(var url: String = "") {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)

        /**
         * http://tools.jb51.net/table/http_content_type/
         */
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

        @JvmStatic
        fun getBasicAuthorization(userName: String, password: String): String {
            return "Basic " + MyUtil.getBase64("${userName}:${password}".toByteArray())
        }

        @JvmStatic
        fun getLoginNamePassword(basicAuthorization: String): LoginNamePasswordData {
            if (basicAuthorization.startsWith("Basic ")) return LoginNamePasswordData();
            var value = MyUtil.getStringContentFromBase64(basicAuthorization.substring("Basic ".length));
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
            return this.status.Between(200, 399)
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
        this.request.requestMethod = "GET"

        if (query.HasValue) {
            var queryObj = JsUtil.parseUrlQueryJson(if (query.startsWith('?')) query else "?" + query)
            var urlObj = JsUtil.parseUrlQueryJson(this.url);
            urlObj.queryJson.putAll(queryObj.queryJson);
            this.url = urlObj.toUrl();
        }
        return doNet()
    }


    fun doPut(postJson: JsonMap): String {
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

        return doPut(requestBody);
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

    @JvmOverloads
    fun doPut(requestBody: String = ""): String {
//        logger.Info { "[post]\t${url}\n${requestHeader.map { it.key + ":" + it.value }.joinToString("\n")}" }

        if (this.request.headers.containsKey("Accept") == false) {
            this.request.headers.set("Accept", "application/json")
        }

        this.request.requestMethod = "PUT"

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

        if (this.request.headers.containsKey("Accept") == false) {
            this.request.headers.set("Accept", "application/json")
        }

        this.request.requestMethod = "POST"

        if (requestBody.HasValue) {
            this.setPostBody(requestBody)
        }

        return doNet()
    }


    /**
     * 信任所有服务器地址,不检查任何证书
     */
    private fun trustAllHttpsHosts() {
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


    fun doNet(): String {
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

            conn.requestMethod = this.request.requestMethod
            if (this.request.chunkedStreamingMode > 0) {
                conn.setChunkedStreamingMode(this.request.chunkedStreamingMode)
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
            if (conn.requestMethod.lowercase().IsIn("post", "put")) {
                conn.doOutput = true


                //如果是 post 小数据
                if (this.request.postBody.any()) {
                    conn.setChunkedStreamingMode(0)

                    DataOutputStream(conn.outputStream).use { out ->
                        out.write(this.request.postBody.toByteArray(const.utf8));
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
                this.response.headers[it.key.lowercase()] = value
            }

            var responseStream: InputStream?
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
            if (this.totalTime.seconds == 0L) {
                this.totalTime = LocalDateTime.now() - startAt
            }


            logger.InfoError(!this.status.Between(200, 399)) {
                var msgs = mutableListOf<String>();
                msgs.add("${conn.requestMethod} ${url}\t[status:${this.status}]");

                msgs.add(this.request.headers.map {
                    return@map "\t${it.key}:${it.value}"
                }.joinToString(const.line_break))

                if (this.status == 0) {
                    msgs.add("[Timeout]");
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
                return@InfoError content;
            }

            conn.disconnect();
        }
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
     * @param filePath:保存位置，优先使用Url中的文件名，如果存在，则用唯一Code命名。
     */
    fun doDownloadFile(filePath: String): FileMessage {
        val CACHESIZE = 1024 * 1024;

        val remoteImage = url;
        val ret = FileMessage();
        val extInfo = FileExtensionInfo.ofUrl(remoteImage);
        if (extInfo.extName.isEmpty()) {
            extInfo.extName = "png";
            extInfo.extType = FileExtensionTypeEnum.Image;
        }

        ret.name = extInfo.name;
        ret.extName = extInfo.extName;

        val oriFile = filePath + File.separatorChar + extInfo.name + "." + extInfo.extName;
        val tempFile = filePath + File.separatorChar + CodeUtil.getCode() + "." + extInfo.extName;

        ret.fullPath = tempFile;

        val destFilePath = File(tempFile)

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

        //判断是否存在原始文件
        if (File(oriFile).exists() == false) {
            File(tempFile).renameTo(File(oriFile));

            ret.fullPath = oriFile;
        }

        return ret;
    }


    /**
     * 大文件上传文件，块大小1MB
     * @param fileName: 文件名。
     * @param fileStream: 文件输入流。
     * @return 上传文件返回的结果，如Json：{id,name,url,msg}
     */
    fun uploadFile(fileName: String, fileStream: InputStream): String {
        val CACHESIZE = 1024 * 1024;

        val boundary = "------" + CodeUtil.getCode();

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
Content-Disposition: form-data; name="${fileName}"; filename="${fileName}"
Content-Type: application/octet-stream

""".replace("\n", "\r\n").toByteArray()
            )


            val bytes = ByteArray(CACHESIZE);
            fileStream.use { input ->
                while (true) {
                    val bytes_len = input.read(bytes)
                    if (bytes_len <= 0) {
                        break;
                    }
                    out.write(bytes, 0, bytes_len)
                }
            }

            out.write("\r\n--${boundary}--".toByteArray())
        }

        val ret = this.doNet()

        if (this.response.resultIsText) {
            logger.info(ret.Slice(0, 4096))
        }

        return ret;
    }
}
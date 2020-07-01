package nbcp.utils

/**
 * Created by udi on 17-4-30.
 */
import nbcp.comm.*
import org.slf4j.LoggerFactory
import java.awt.image.BufferedImage
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
import java.io.IOException
import java.lang.RuntimeException
import java.time.LocalDateTime
import java.util.*
import javax.imageio.ImageIO


data class FileMessage(
        var fullPath: String = "",
        var name: String = "",
        var extName: String = "",
        var msg: String = "");

//data class HttpReturnData(
//        var url: String = "",
//        var status: Int = 0,
//        var contentType: String = "",
//        var header: StringMap = StringMap(),
//        var data: ByteArray = byteArrayOf()
//)
/*
 * 利用HttpClient进行post请求的工具类
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


    var responseResult: ByteArray = byteArrayOf()
    private var requestActions: MutableList<((HttpURLConnection) -> Unit)> = mutableListOf()
    private var responseActions: MutableList<((HttpURLConnection) -> Unit)> = mutableListOf()

    private var postBody = byteArrayOf()

    /**
     * 回发的编码，只读
     */
    var responseCharset: String = ""
        private set;

    /**
     * 该次回发Header，只读 ,全小写
     */
    var responseHeader: StringMap = StringMap()
        private set;

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

    fun setPostBody(postBody: ByteArray): HttpUtil {
        this.postBody = postBody;
        return this;
    }

    fun setPostBody(postBody: String): HttpUtil {
        return this.setPostBody(postBody.toByteArray(utf8))
    }
//    private var https = false;
//
//    fun setHttps(https:Boolean):HttpUtil{
//        this.https = https;
//        return this;
//    }

    fun setRequestHeader(key: String, value: String): HttpUtil {
        this.setRequest { it.setRequestProperty(key, value) }
        return this;
    }

    fun setRequest(action: ((HttpURLConnection) -> Unit)): HttpUtil {
        this.requestActions.add(action)
        return this;
    }

    fun setResponse(action: ((HttpURLConnection) -> Unit)): HttpUtil {
        this.responseActions.add(action);
        return this;
    }

    fun doGet(): String {
        this.setRequest { it.requestMethod = "GET" }

        var retData = doNet()

        return retData.toString(Charset.forName(responseCharset.AsString("UTF-8")));
    }

    /**
     * Post请求
     */
    fun doPost(postJson: JsonMap): String {

        this.setRequest {
            if (it.requestProperties.containsKey("Accept") == false) {
                it.setRequestProperty("Accept", "application/json")
            }
            if (it.requestProperties.containsKey("Content-Type") == false) {
                it.setRequestProperty("Content-Type", "application/json;charset=UTF-8")
            }

            var requestBody = "";
            if (it.getRequestProperty("Content-Type").AsString().contains("json") == false) {
                requestBody = postJson.map { it.key + "=" + it.value }.joinToString("&");
            } else {
                requestBody = postJson.ToJson();
            }

            this.setPostBody(requestBody)
        }

        return doPost();
    }

    /**
     * Post请求
     */
    fun doPost(requestBody: String = ""): String {
//        logger.Info { "[post]\t${url}\n${requestHeader.map { it.key + ":" + it.value }.joinToString("\n")}" }

        this.setRequest { it.requestMethod = "POST" }

        if (requestBody.HasValue) {
            this.setPostBody(requestBody)
        }

        var ret = doNet()
//        { conn ->
//            if (requestBody.HasValue) {
//                logger.Info { "\t[post_body]${requestBody}" }
//                //conn.setRequestProperty("Content-Length", requestBody.toByteArray().size.toString());
//                //POST请求
//                var out = OutputStreamWriter(conn.outputStream);
//                out.write(requestBody);
//
//                out.flush();
//                out.close();
//            }
//        }

//        if (isTxt) {
//            logger.info(ret.Slice(0, 4096));
//        }


        return ret.toString(Charset.forName(responseCharset.AsString("UTF-8")));
    }

    private var requestProperties: StringMap = StringMap();
    fun doNet(): ByteArray {
        var conn: HttpURLConnection? = null

        this.requestProperties.clear();
//        var lines = mutableListOf<String>()

        var requestBodyValidate = false;

        var startAt = LocalDateTime.now();

        try {
            conn = URL(url).openConnection() as HttpURLConnection;

            //建立连接
            conn.instanceFollowRedirects = false

            conn.useCaches = false
            conn.connectTimeout = 3000;
            conn.readTimeout = 5000;

            conn.setRequestProperty("Connection", "close")


            this.requestActions.forEach {
                it.invoke(conn!!);
            }

            if (conn.requestMethod.isNullOrEmpty()) {
                throw RuntimeException("没有设置 method！");
            }

            if (conn.requestMethod.toLowerCase().IsIn("post", "put")) {
                requestBodyValidate = true;
            }

            conn.doInput = true

            //https://bbs.csdn.net/topics/290053257
            //GET,HEAD,OPTIONS
            if (requestBodyValidate) {
                conn.doOutput = true
                conn.setChunkedStreamingMode(0)
            }

            conn.requestProperties.forEach { k, v ->
                if (k == null) {
                    return@forEach;
                }
                this.requestProperties.put(k, v.joinToString(","))
            }

//            conn.connect();

            if (requestBodyValidate && this.postBody.any()) {
                //POST数据

                var out = DataOutputStream(conn.outputStream);
                try {
                    out.write(this.postBody);
                    out.flush();
                } catch (e: Exception) {
                    throw e;
                } finally {
                    out.close()
                }
            }

            this.status = conn.responseCode

            conn.headerFields.forEach {
                if (it.key == null) {
                    return@forEach
                }
                var value = it.value.joinToString(",")
                this.responseHeader[it.key.toLowerCase()] = value
            }

            this.responseActions.forEach {
                it.invoke(conn!!);
            }

            var char_parts = conn.contentType.AsString().split(";").last().split("=");
            if (char_parts.size == 2) {
                if (char_parts[0].trim().VbSame("charset")) {
                    responseCharset = char_parts[1];
                }
            }

            try {
                var input = conn.inputStream;
                try {
                    this.responseResult = toByteArray(input);
                } catch (e: Exception) {
                    throw e;
                } finally {
                    input.close()
                }

                this.totalTime = LocalDateTime.now() - startAt
                return this.responseResult;
            } catch (e: Exception) {
                msg = e.message ?: "服务器错误"
                return "".toByteArray(utf8);
            }
            //读取响应
        } catch (e: Exception) {
            msg = e.message ?: "请求错误"
            return "".toByteArray(utf8);
        } finally {
            // 断开连接
            if (this.totalTime.totalMilliseconds == 0L) {
                this.totalTime = LocalDateTime.now() - startAt
            }

            if (conn != null) {
                logger.InfoError(this.status != 200) {
                    var msgs = mutableListOf<String>();
                    msgs.add("${conn!!.requestMethod} ${url}\t[status:${this.status}]");

                    this.requestProperties.map {
                        return@map "\t${it.key}:${it.value}"
                    }.joinToString(line_break)

                    //小于 10K
                    if (postBody.any() && postBody.size < 10240) {
                        msgs.add(postBody.toString(utf8))
                    }

                    msgs.add("---")

                    this.responseHeader.map {
                        return@map "\t${it.key}:${it.value}"
                    }.joinToString(line_break)


                    //小于10K
                    if (this.responseCharset.HasValue && this.responseResult.size < 10240) {
                        msgs.add(this.responseResult.toString(Charset.forName(this.responseCharset)))
                    }

                    var content = msgs.joinToString(line_break);
                    msgs.clear();
                    return@InfoError content;
                }
            }


            conn?.disconnect();
            conn = null;
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

        this.setRequest { it.requestMethod = "GET" }

        var retData = doNet();

        destFilePath.appendBytes(retData);

        //判断是否存在原始文件
        if (File(oriFile).exists() == false) {
            File(tempFile).renameTo(File(oriFile));

            ret.fullPath = oriFile;
        }

        return ret;
    }


    /**
     * 上传文件
     * @param filePath: 要上传的文件。
     */
    fun uploadFile(filePath: String): String {
        var file = File(filePath);
        if (file.exists() == false) {
            throw  Exception("文件${filePath}不存在")
        }

        var fileName = file.name;

        var boundary = "------" + CodeUtil.getCode();

        this.setRequest {
            it.setRequestProperty("Connection", "keep-alive")
            it.setRequestProperty("Content-Type", "multipart/form-data; boundary=${boundary}")
        }

        var b_file = FileInputStream(filePath)


        var content = mutableListOf<Byte>()
        content.addAll("""--${boundary}
Content-Disposition: form-data; name="${fileName}"; filename="blob"
Content-Type: application/octet-stream

""".replace("\n", "\r\n").toByteArray().toList())

        content.addAll(b_file.readBytes().toList())

        content.addAll("\r\n--${boundary}--".toByteArray().toList())

        this.setRequest { it.requestMethod = "POST" }

        var isTxt = false;
        this.setResponse { conn ->
            isTxt = conn.contentType.contains("json", true) || conn.contentType.contains("htm", true) || conn.contentType.contains("text", true)
        }

        this.setPostBody(content.toByteArray())

        var ret = this.doNet()
//        { conn ->

//            if (content.size > 0) {
//                //conn.setRequestProperty("Content-Length", requestBody.toByteArray().size.toString());
//                //POST请求
//                var out = DataOutputStream(conn.outputStream);
//                out.write();
//
//                out.flush();
//                out.close();
//            }
//        }
                .toString(Charset.forName(responseCharset))

        if (isTxt) {
            logger.info(ret.Slice(0, 4096))
        }
        return ret;
    }
}
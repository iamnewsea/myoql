package nbcp.base.utils

/**
 * Created by udi on 17-4-30.
 */
import com.sun.net.ssl.HttpsURLConnection
import nbcp.comm.*
import org.slf4j.LoggerFactory
import nbcp.comm.*
import nbcp.base.extend.*
import java.awt.image.BufferedImage
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
import java.io.IOException
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


    var responseResult: ByteArray = byteArrayOf(0)
    private var requestAction: ((HttpURLConnection) -> Unit)? = null
    private var responseAction: ((HttpURLConnection) -> Unit)? = null
    val requestHeader: StringMap = StringMap()
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
        this.requestHeader.set(key, value);
        return this;
    }

    fun setRequest(action: ((HttpURLConnection) -> Unit)): HttpUtil {
        this.requestAction = action
        return this;
    }

    fun setResponse(action: ((HttpURLConnection) -> Unit)): HttpUtil {
        this.responseAction = action
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
        if (requestHeader.containsKey("Accept") == false) {
            requestHeader["Accept"] = "application/json";
        }

        if (requestHeader.containsKey("Content-Type") == false) {
            requestHeader["Content-Type"] = "application/json;charset=UTF-8";
        }

        var requestBody = "";
        if (requestHeader["Content-Type"].toString().indexOf("json") < 0) {
            requestBody = postJson.map { it.key + "=" + it.value }.joinToString("&");
        } else {
            requestBody = postJson.ToJson();
        }

        return doPost(requestBody);
    }

    /**
     * Post请求
     */
    fun doPost(requestBody: String): String {
//        logger.Info { "[post]\t${url}\n${requestHeader.map { it.key + ":" + it.value }.joinToString("\n")}" }

        this.setRequest { it.requestMethod = "POST" }

        this.setPostBody(requestBody)

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

    fun doNet(): ByteArray {
        var conn: HttpURLConnection? = null;
//        var lines = mutableListOf<String>()
        try {
            //建立连接
            conn = URL(url).openConnection() as HttpURLConnection;
            conn.requestMethod = "POST"
            conn.instanceFollowRedirects = false
            conn.doInput = true
            conn.doOutput = true
            conn.useCaches = false

            //如果获取 conn!!.contentType，则 content-length 就为0
            requestHeader.forEach {
                conn.setRequestProperty(it.key, it.value);
            }

            if (this.requestAction != null) {
                this.requestAction!!.invoke(conn)
            }

            conn.connect();

            if (this.postBody.any()) {
                //POST数据
                var out = DataOutputStream(conn.outputStream);
                out.write(this.postBody);

                out.flush();
                out.close();
            }

            this.status = conn.responseCode

            conn.headerFields.forEach {
                if (it.key == null) {
                    return@forEach
                }
                var value = it.value.joinToString(",")
                this.responseHeader[it.key.toLowerCase()] = value
            }

            if (this.responseAction != null) {
                this.responseAction!!.invoke(conn)
            }


            this.setResponse { conn ->
                var char_parts = conn.contentType.AsString().split(";").last().split("=");
                if (char_parts.size == 2) {
                    if (char_parts[0].trim().VbSame("charset")) {
                        responseCharset = char_parts[1];
                    }
                }
            }

            try {
                this.responseResult = toByteArray(conn.getInputStream());
                return this.responseResult;
            } catch (e: Exception) {
                msg = e.message ?: "服务器错误"
                return "".toByteArray(utf8);
            }
            //读取响应
        } catch (e: Exception) {
            msg = e.message ?: "请求错误"
            throw e;
        } finally {
            try {
                if (conn != null) {
                    logger.InfoError(this.status != 200) {
                        var msgs = mutableListOf<String>();
                        msgs.add("${conn.requestMethod} ${url}\t[status:${this.status}]");

                        this.requestHeader.map {
                            if (it.key == null) {
                                return@map "\t${it.value}"
                            }
                            return@map "\t${it.key}:${it.value}"
                        }.joinToString(line_break)
                                .apply {
                                    if (this.HasValue) {
                                        msgs.add(this);
                                    }
                                }

                        //小于 10K
                        if (postBody.any() && postBody.size < 10240) {
                            msgs.add(postBody.toString(utf8))
                        }

                        msgs.add("---")

                        this.responseHeader.map {
                            if (it.key == null) {
                                return@map "\t${it.value}"
                            }
                            return@map "\t${it.key}:${it.value}"
                        }.joinToString(line_break)
                                .apply {
                                    if (this.HasValue) {
                                        msgs.add(this);
                                    }
                                }

                        //小于10K
                        if (this.responseCharset.HasValue && this.responseResult.size < 10240) {
                            msgs.add(this.responseResult.toString(Charset.forName(this.responseCharset)))
                        }
                        return@InfoError msgs.joinToString(line_break)
                    }
                }
            } finally {

            }


            if (conn != null) {
                // 断开连接
                conn.disconnect();
            }
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

        this.requestHeader["Connection"] = "keep-alive"
        this.requestHeader["Content-Type"] = "multipart/form-data; boundary=${boundary}"


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
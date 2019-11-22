package nbcp.base.utils

/**
 * Created by udi on 17-4-30.
 */
import nbcp.base.comm.StringMap
import org.slf4j.LoggerFactory
import nbcp.base.comm.JsonMap
import nbcp.base.extend.*
import nbcp.base.utf8
import java.awt.image.BufferedImage
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
import java.io.IOException
import java.util.*
import javax.imageio.ImageIO


/*
 * 利用HttpClient进行post请求的工具类
 */

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

class HttpUtil(var url: String = "") {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
        //远程下载图片,并压缩
        /**
         * @param imagePath :图片的目录,系统下载后, 会先尝试 remoteImage名字进行保存,如果失败,则使用唯一Id进行保存.
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

    var charset: String = "utf-8";
    var requestHeader: StringMap = StringMap()
    var responseHeader: StringMap = StringMap();

    var status: Int = 0;
    var msg: String = ""; //初始化失败的消息.用于对象传递

    fun doGet(charsetCallback: ((HttpURLConnection) -> Charset)? = null): String {
        var charset = utf8
        var retData = doNet({ it.requestMethod = "GET" }
        , {}, { conn ->
            if( charsetCallback != null){
                charset = charsetCallback.invoke(conn);
            }
        })

        return retData.toString(charset);
    }

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

    fun doPost(requestBody: String,charsetCallback: ((HttpURLConnection) -> Charset)? = null ): String {
        logger.info("[post]\t${url}\n${requestHeader.map { it.key + ":" + it.value }.joinToString("\n")}")

        var charset = utf8
//        var isTxt = false;
        var ret = doNet({ it.requestMethod = "POST" }, {
            if (requestBody.HasValue) {
                logger.info("\t[post_body]${requestBody}")
                //conn.setRequestProperty("Content-Length", requestBody.toByteArray().size.toString());
                //POST请求
                var out = OutputStreamWriter(it.outputStream);
                out.write(requestBody);

                out.flush();
                out.close();
            }
        }
        , { conn ->
            if( charsetCallback != null){
                charset = charsetCallback.invoke(conn);
            }
        })

//        if (isTxt) {
//            logger.info(ret.Slice(0, 4096));
//        }

        return ret.toString(charset);
    }

    private fun doNet(preSet: ((HttpURLConnection) -> Unit), postBody: ((HttpURLConnection) -> Unit), postBack: ((HttpURLConnection) -> Unit)): ByteArray {
        var conn: HttpURLConnection? = null;
        var lines = mutableListOf<String>()
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

            preSet(conn);

            conn.connect();

            postBody(conn)

            this.status = conn.responseCode

            if (this.status != 200) {
                logger.error("${conn.requestMethod} ${url}\t[status:${this.status}]")
            } else {
                logger.info("${conn.requestMethod} ${url}\t[status:${this.status}]")
            }

            logger.info(conn.headerFields.map {
                if (it.key == null) {
                    return@map "\t${it.value.joinToString(",")}"
                }
                return@map "\t${it.key}:${it.value}"
            }.joinToString("\n"))

            conn.headerFields.forEach {
                if (it.key == null) {
                    return@forEach
                }
                var value = it.value.joinToString(",")
                this.responseHeader[it.key] = value
            }

            postBack(conn);
            try {
                return toByteArray(conn.getInputStream());
            } catch (e: Exception) {
                return (e.message ?: "").toByteArray(utf8);
            }
            //读取响应
        } catch (e: Exception) {
            logger.error(e.message ?: "网络请求错误!")
            throw e;
        } finally {
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

    //返回错误信息
    fun doGetFile(filePath: String): FileMessage {
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


        var retData = doNet({ it.requestMethod = "GET" }, {}, {});

        destFilePath.appendBytes(retData);

        //判断是否存在原始文件
        if (File(oriFile).exists() == false) {
            File(tempFile).renameTo(File(oriFile));

            ret.fullPath = oriFile;
        }

        return ret;
    }


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

        var isTxt = false;
        var ret = this.doNet({ it.requestMethod = "POST" }, {
            if (content.size > 0) {
                //conn.setRequestProperty("Content-Length", requestBody.toByteArray().size.toString());
                //POST请求
                var out = DataOutputStream(it.outputStream);
                out.write(content.toByteArray());

                out.flush();
                out.close();
            }
        }, { conn ->
            isTxt = conn.contentType.contains("json", true) || conn.contentType.contains("htm", true) || conn.contentType.contains("text", true)
        }).toString(utf8)

        if (isTxt) {
            logger.info(ret.Slice(0, 4096))
        }
        return ret;
    }
}
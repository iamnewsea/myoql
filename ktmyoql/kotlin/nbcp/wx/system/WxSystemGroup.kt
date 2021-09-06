package nbcp.wx.system

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64
import nbcp.comm.*
import nbcp.db.db
import nbcp.utils.HttpUtil
import nbcp.utils.Md5Util
import nbcp.utils.SpringUtil
import nbcp.wx.wx
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.lang.RuntimeException
import java.nio.charset.Charset
import java.security.AlgorithmParameters
import java.security.Security
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object WxSystemGroup {

    /**
     * 微信签名
     * https://pay.weixin.qq.com/wiki/doc/api/wxa/wxa_api.php?chapter=4_3
     * 忽略 @Ignore 字段， 一般是 sign 字段。
     * 如果 指定了 @Require ，则要求不能为空。
     */
    fun sign(mchSecret: String, wxModel: Any): String {
        var type = wxModel::class.java
        var list = type.AllFields
            .sortedBy { it.name }
            .map {
                if (it.getAnnotation(Ignore::class.java) != null) {
                    return@map ""
                }

                var require = it.getAnnotation(Require::class.java) != null

                var value = it.get(wxModel)

                if (value == null) {
                    if (require) {
                        throw RuntimeException("微信小程序签名时，${it.name} 不能为空值");
                    }
                    return@map "";
                }
                if (value is String && value.isNullOrEmpty()) {
                    if (require) {
                        throw RuntimeException("微信小程序签名时，${it.name} 为必填项");
                    }
                    return@map ""
                }

                return@map it.name + "=" + value
            }
            .filter { it.isNotEmpty() }
            .toMutableList();

        list.add("key=${mchSecret}");
        return Md5Util.getMd5(list.joinToString("&")).toUpperCase();
    }


    fun toXml(mchSecret: String, wxModel: Any): String {
        var sign = sign(mchSecret, wxModel);
        if (sign.isEmpty()) return "";

        var type = wxModel::class.java;

        return "<xml>" + type.AllFields
            .sortedBy { it.name }
            .map {

                var value = it.get(wxModel)
                if (value == null) {
                    return@map "" to ""
                }

                if (value is String && value.isEmpty()) {
                    return@map "" to ""
                }

                return@map it.name to value
            }
            .filter { it.first.isNotEmpty() }
            .toMutableList()
            .apply {
                add("sign" to sign)
            }
            .map {
                return@map "<${it.first}><![CDATA[${it.second}]]></${it.first}>"
            }
            .joinToString("") + "</xml>";
    }


    /**
     * https://www.cnblogs.com/handsomejunhong/p/8670367.html
     * 解密用户的加密数据
     */
    fun decryptWxUserData(encryptedData: String, sessionKey: String, iv: String): String { // 被加密的数据
        val dataByte = Base64.decode(encryptedData)
        // 加密秘钥
        var keyByte = Base64.decode(sessionKey)
        // 偏移量
        val ivByte = Base64.decode(iv)
        // 如果密钥不足16位，那么就补足.  这个if 中的内容很重要
        val base = 16
        if (keyByte.size % base != 0) {
            val groups = keyByte.size / base + if (keyByte.size % base != 0) 1 else 0
            val temp = ByteArray(groups * base)
            Arrays.fill(temp, 0.toByte())
            System.arraycopy(keyByte, 0, temp, 0, keyByte.size)
            keyByte = temp
        }
        // 初始化
        Security.addProvider(BouncyCastleProvider())
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC")
        val spec = SecretKeySpec(keyByte, "AES")
        val parameters = AlgorithmParameters.getInstance("AES")
        parameters.init(IvParameterSpec(ivByte))
        cipher.init(Cipher.DECRYPT_MODE, spec, parameters) // 初始化
        val resultByte = cipher.doFinal(dataByte)
        if (null == resultByte || resultByte.size <= 0) {
            return "";
        }
        return String(resultByte, const.utf8)
    }

    /**
     * 生成带参数二维码,C端展示  base64
     * https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/qr-code/wxacode.getUnlimited.html
     */
    @JvmOverloads
    fun getMiniCode(appSecret: String, scene: String, page: String, width: Int = 0): ApiResult<HttpUtil> {
        require(appSecret.HasValue) { "缺少appSecret!" }

        //获取token
        val tokenData = wx.officeAccount.getAccessToken(appSecret)

        if (tokenData.msg.HasValue) {
            return ApiResult(tokenData.msg)
        }

        val token = tokenData.data!!.token

        val requestUrl = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=${token}"

        var postBody = JsonMap();
        postBody["scene"] = scene;
        postBody["page"] = page;
        if (width > 0) {
            postBody["width"] = width;
        }

        var http = HttpUtil(requestUrl)
        http.request.requestMethod == "POST"
        http.setPostBody(postBody.ToJson());

        var bytes = http.doNet();
        if (http.status != 200 || http.response.contentType.contains("json")) {
            return ApiResult(bytes)
        }

        return ApiResult.of(http)

//        // 请求返回来的流接收到这里,然后转换
//        val outStream = ByteArrayOutputStream()
//
//        try {
//            val url = URL(requestUrl)
//            val httpURLConnection = url.openConnection() as HttpURLConnection
//
//            httpURLConnection.requestMethod = "POST";
//            httpURLConnection.doOutput = true;
//            httpURLConnection.doInput = true;
//
//            val printWriter = PrintWriter(httpURLConnection.outputStream)

//            printWriter.write(params.ToJson());
//
//            printWriter.flush();
//
//            //开始获取数据
//            val bis = BufferedInputStream(httpURLConnection.inputStream)
//            //保存为文件时候使用,目前只需要显示
//            //val os = FileOutputStream(File("C:\\Users\\c\\Desktop\\abc.png"))
//            try {
//                var read: Int = -1
//                bis.use { input ->
//                    /*保存文件方法
//                    os.use {
//                        while ({ read = input.read();read }() != -1) {
//                            it.write(read)
//
//                        }
//                    }*/
//                    outStream.use {
//                        while ({ read = input.read();read }() != -1) {
//                            it.write(read)
//
//                        }
//                    }
//                }
//
//            } catch (t: Throwable) {
//                return ApiResult("写入图片流出错$t")
//            }
//
////但是在kotlin中等式不是一个表达式，所以不能那样子写，kotlin是这样的使用的，有几种写法：
////在使用流或者数据库之类的资源需要关闭close的情况下，可以使用use扩展函数来实现自动关闭的操作
////            while ((len = bis.read(arr)) != -1)
////            {
////                os.write(arr, 0, len);
////                os.flush();
////            }
//            //also 写法
//            /*try {
//                var read: Int = -1
//                `bis`.use { input ->
//                    os.use {
//                        while (input.read().also { read = it } != -1) {
//                            it.write(read)
//                        }
//                    }
//                }
//            } catch (t: Throwable) {
//                t.printStackTrace()
//            }
//*/
//        } catch (e: Exception) {
//            return ApiResult("获取图片出错$e")
//        }
//
//        if (outStream.size() <100){
//            //val map = outStream.toString().FromJson<HashMap<Any,Any>>()
//            return ApiResult(outStream.toString())
//        }
//
//        //处理数据
//        val b64 = BASE64Encoder().encode(outStream.toByteArray())
//        return ApiResult.of(data = "data:image/png;base64,$b64")
    }

    /**
     * 推送消息
     */
    fun pushMessage(data: wx_msg_data, appSecret: String): ApiResult<String> {
        require(appSecret.HasValue) { "缺少appSecret!" }

        var wx_url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=";

        var tokenData = wx.officeAccount.getAccessToken(appSecret)

        var url = HttpUtil();
        if (tokenData.msg.HasValue) {
            return ApiResult(tokenData.msg)
        }

        url.url = "${wx_url}${tokenData.data!!.token}"
        url.request.contentType = "application/json"

        var ret = url.doPost(data.ToJson()).FromJson<wx_return_data>() ?: wx_return_data()
        if (ret.errcode != 0) {
            return ApiResult(ret.errmsg);
        }
        return ApiResult();
    }

}
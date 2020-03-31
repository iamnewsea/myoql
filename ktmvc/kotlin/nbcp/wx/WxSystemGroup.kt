package nbcp.wx

import nbcp.base.extend.AsString
import nbcp.base.extend.FromJson
import nbcp.base.extend.HasValue
import nbcp.base.extend.ToJson
import nbcp.base.utils.HttpUtil
import nbcp.base.utils.SpringUtil
import nbcp.comm.ApiResult
import nbcp.comm.JsonMap
import nbcp.comm.StringMap
import nbcp.comm.utf8
import nbcp.db.db
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import sun.misc.BASE64Encoder
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintWriter
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset


object WxSystemGroup {


    /**
     * 生成带参数二维码,C端展示  base64
     * https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/qr-code/wxacode.getUnlimited.html
     */
    fun getMiniCode(appSecret: String, scene: String, page: String, width: Int = 0): ApiResult<ByteArray> {
        var appId = SpringUtil.context.environment.getProperty("app.wx.appId")

        //获取token
        val tokenData = db.rer_base.wx.getAccessToken(appId, appSecret)

        if (tokenData.msg.HasValue) {
            println(tokenData.msg)
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
                .setRequest { it.requestMethod == "POST" }
                .setPostBody(postBody.ToJson());
        var bytes = http.doNet();
        if (http.status != 200 || http.responseHeader.getKeyIgnoreCase("content-type").AsString().contains("json")) {
            return ApiResult(bytes.toString(Charset.forName(http.responseCharset.AsString("UTF-8"))))
        }
        return ApiResult.of(bytes)

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
        var wx_url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=";

        var appId = SpringUtil.context.environment.getProperty("app.wx.appId")
        var tokenData = db.rer_base.wx.getAccessToken(appId, appSecret)

        var url = HttpUtil();
        if (tokenData.msg.HasValue) {
            return ApiResult(tokenData.msg)
        }

        url.url = "${wx_url}${tokenData.data!!.token}"
        url.requestHeader["Content-Type"] = MediaType.APPLICATION_JSON_VALUE

        var ret = url.doPost(data.ToJson()).FromJson<wx_msg_return_data>() ?: wx_msg_return_data()
        if (ret.errcode != 0) {
            return ApiResult(ret.errmsg);
        }
        return ApiResult();
    }
}


data class wx_msg_return_data(
        var errcode: Int = 0,
        var errmsg: String = ""
)

data class wx_msg_data_value(
        var value: String = "",
        var color: String = "#000000"
)

data class wx_msg_data(
        var touser: String = "",// 	是 	接收者（用户）的 openid
        var template_id: String = "",// 	是 	所需下发的模板消息的id
        var page: String = "",// 	否 	点击模板卡片后的跳转页面，仅限本小程序内的页面。支持带参数,（示例index?foo=bar）。该字段不填则模板无跳转。
        //var form_id: String = "",// 	是 	表单提交场景下，为 submit 事件带上的 formId；支付场景下，为本次支付的 prepay_id
        var data: LinkedHashMap<String, wx_msg_data_value> = LinkedHashMap<String, wx_msg_data_value>()// 	是 	模板内容，不填则下发空模板
)
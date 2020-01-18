package nbcp.handler

import nbcp.comm.*
import nbcp.comm.*
import nbcp.comm.*
import nbcp.base.extend.*
import nbcp.base.utf8
import nbcp.base.utils.HttpUtil
import nbcp.web.MyHttpRequestWrapper
import java.nio.charset.Charset
import java.util.LinkedHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Created by yuxh on 2018/7/26
 */
data class RequestDataModel(
        var url: String
) {
    var json = JsonMap()
    var contentType = "application/json;charset=UTF-8"
    var method = "POST"
        set(method) {
            field = method.toUpperCase()
        }
}

data class ResponseDataModel(
        var status: Int,
        var header: StringMap = StringMap(),
        var body: String
) {
}

class RequestTask(internal var request: RequestDataModel, internal var index: Int, internal var callback: (Int, HttpUtil, String) -> Unit) : Runnable {

    override fun run() {
        var ajax = HttpUtil(request.url)
        var response = ""
        if ("GET" == request.method) {
            response = ajax.doGet()
        } else if ("POST" == request.method) {
            if (request.contentType.isNotEmpty()) {
                ajax.requestHeader["Content-Type"] = request.contentType
            }
            response = ajax.doPost(request.json)
        }

        this.callback(this.index, ajax, response)
    }
}

/**
 * 一次执行多个请求，返回格式：
 * [
 *  { status:200 , header, body },
 *  { status:302 , header, body }
 * ]
 *
 * 每项 header 的值 会 减去 最外面的 header 相同的值。
 */
@WebServlet(urlPatterns = arrayOf("/ajax/group"))
@OpenAction
class GroupAjax : HttpServlet() {
    override fun doPost(request: HttpServletRequest, resp: HttpServletResponse) {
        if (request is MyHttpRequestWrapper == false) {
            return
        }

        var json = (request.body ?: byteArrayOf()).toString(utf8).trim()
        var resp_text = groupAjax(json.FromJsonWithDefaultValue())
        if (resp_text.isNotEmpty()) {
            resp.contentType = "application/json;charset=UTF-8"
            resp.outputStream.write(resp_text.toByteArray(utf8))
        }
    }

    fun groupAjax(requestDatas: List<RequestDataModel>): String {
        val ret = arrayOfNulls<ResponseDataModel>(requestDatas.size)
        val executor = Executors.newFixedThreadPool(requestDatas.size)
        var index: Int = -1
        for (request in requestDatas) {
            index++

            executor.execute(RequestTask(request, index, { index2, ajax, response ->
                ret.set(index2, ResponseDataModel(ajax.status, ajax.responseHeader, response))
            }))
        }

        executor.shutdown()
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS)
        } catch (e: InterruptedException) {
            e.printStackTrace()
            return "[]";
        }

        return "[" + ret.map {
            """{"status":${it!!.status},"header":${it.header.ToJsonValue()},"body": ${it.body.ToJsonValue()}"""
        }.joinToString(",") + "]"
    }
}




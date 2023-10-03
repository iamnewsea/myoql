@file:JvmName("MyWebHelper")
@file:JvmMultifileClass

package nbcp.web.util

import nbcp.base.comm.*
import nbcp.base.db.*
import nbcp.base.enums.*
import nbcp.base.extend.*
import nbcp.base.utils.*
import org.slf4j.LoggerFactory
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

object MyWebUtil {
    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * 把当前请求转向另一个目标，并把目标结果输出。
     */
    @JvmStatic
    fun transform(
        request: HttpServletRequest,
        response: HttpServletResponse,
        targetUrl: String
    ): HttpUtil {
        var http = HttpUtil(targetUrl)
        http.request.httpMethod = request.method.ToEnum(HttpMethod::class.java)!!
        if (request.method basicSame "POST" || request.method basicSame "PUT") {
            http.setPostBody(request.inputStream.readContentString())
        }

        run {
            var headerArray = arrayOf("accept", "content-type")
            var requestHeaderNames = request.headerNames.toList()
            headerArray.forEach { headerName ->
                var requestHeaderName = requestHeaderNames.firstOrNull { headerName basicSame it }
                if (requestHeaderName != null) {
                    request.getHeader(requestHeaderName).apply {
                        http.request.headers.put(requestHeaderName, this)
                    }
                }
            }
        }


        var res = http.doNet()
        response.status = http.status;

        run {
            var headerArray = arrayOf("content-type")
            var responseHeaderNames = http.response.headers.keys.toList()
            headerArray.forEach { headerName ->
                var responseHeaderName = responseHeaderNames.firstOrNull { headerName basicSame it }
                if (responseHeaderName != null) {
                    response.setHeader(
                        responseHeaderName,
                        http.response.headers.get(responseHeaderName)
                    )
                }
            }
        }

        response.writer.write(res)
        return http
    }
}


//fun <M : MongoBaseMetaCollection<out E>, E : Any> MongoSetEntityUpdateClip<M, E>.withRequestParams(): MongoSetEntityUpdateClip<M, E> {
//    return this.withRequestJson(HttpContext.request.getPostJson())
//}

//fun <M : SqlBaseMetaTable<out Serializable>> SqlSetEntityUpdateClip<M>.withRequestParams(): SqlSetEntityUpdateClip<M> {
//    var columns = this.mainEntity.getColumns();
//    HttpContext.request.requestParameterKeys .forEach { key ->
//        var column = columns.firstOrNull { it.name == key }
//        if (column != null) {
//            this.withColumn { column }
//        }
//    }
//    return this
//}

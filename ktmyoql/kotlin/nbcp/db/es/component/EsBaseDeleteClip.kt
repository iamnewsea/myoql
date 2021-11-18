package nbcp.db.es

import nbcp.comm.*
import nbcp.db.MyOqlOrmScope
import nbcp.utils.*
import nbcp.db.db
import nbcp.db.es.*
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.elasticsearch.client.Request
import org.elasticsearch.client.Response
import org.slf4j.LoggerFactory
import java.lang.RuntimeException
import java.time.LocalDateTime
import nbcp.scope.*

open class EsBaseDeleteClip(tableName: String) : EsClipBase(tableName), IEsWhereable {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    var routing = ""
    var pipeline = ""
    var refresh: EsPutRefreshEnum? = null
    var ids = mutableListOf<String>()

    /**
     * 批量添加中的添加实体。
     */
    fun addId(id: String) {
        if (id.isEmpty()) {
            throw RuntimeException("批量删除时需要指定Id")
        }

        this.ids.add(id)
    }

    fun withRouting(routeing: String) {
        this.routing = routeing;
    }

    fun withPipeLine(pipeline: String) {
        this.pipeline = pipeline;
    }

    fun withRefresh(refresh: EsPutRefreshEnum) {
        this.refresh = refresh;
    }

    /**
     * 批量插入
     * https://www.elastic.co/guide/en/elasticsearch/reference/7.6/docs-bulk.html
     */
    fun exec(): Int {
        db.affectRowCount = -1;
        var ret = -1;

        var settingResult = db.es.esEvents.onDeleting(this)
        if (settingResult.any { it.second.result == false }) {
            return 0;
        }

        var search = JsonMap();
        if (this.refresh != null) {
            search.put("refresh", this.refresh.toString())
        }
        if (this.pipeline.HasValue) {
            search.put("pipeline", this.pipeline)
        }
        if (this.routing.HasValue) {
            search.put("routing", this.routing)
        }

        var request = Request("POST", "/_bulk" +
            search.toUrlQuery().IfHasValue { "?" + it }
        )

        var data = mutableListOf<Any>()
        this.ids.forEach { id ->
            if (id.isNullOrEmpty()) {
                throw RuntimeException("更新实体缺少 id值")
            }

            data.add(JsonMap("delete" to JsonMap("_index" to this.collectionName, "_id" to id)))
        }

        var requestBody = "";
        usingScope(arrayOf(JsonStyleEnumScope.DateUtcStyle, JsonStyleEnumScope.Compress)) {
            requestBody = data.map { it.ToJson() + const.line_break }.joinToString("")
        }

        request.entity = NStringEntity(requestBody, ContentType.create("application/x-ndjson", const.utf8))

//        var responseBody = "";
        val startAt = LocalDateTime.now()
        var error: Exception? = null
        var response: Response? = null
        try {
            response = esTemplate.performRequest(request)
            if (response.statusLine.statusCode != 200) {
                return ret;
            }

            db.executeTime = LocalDateTime.now() - startAt
//            responseBody = response.entity.content.readBytes().toString(const.utf8)

            usingScope(arrayOf(MyOqlOrmScope.IgnoreAffectRow, MyOqlOrmScope.IgnoreExecuteTime)) {
                settingResult.forEach {
                    it.first.delete(this, it.second)
                }
            }

//            ret = ids.size;
            db.affectRowCount = ids.size
            return db.affectRowCount
        } catch (e: Exception) {
            error = e;
            throw e;
        } finally {
//            response.entity.content.ReadContentStringFromStream()
            EsLogger.logDelete(
                error, collectionName, request,
                response?.statusLine?.statusCode.AsString() + "," + ids.size
            )
        }

//        return ret;
    }
}
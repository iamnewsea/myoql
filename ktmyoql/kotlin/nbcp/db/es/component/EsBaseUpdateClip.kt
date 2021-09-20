package nbcp.db.es

import nbcp.comm.*
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

open class EsBaseUpdateClip(tableName: String) : EsClipBase(tableName), IEsWhereable {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)

    }


    var routing = ""
    var pipeline = ""
    var refresh: EsPutRefreshEnum? = null
    var entities = mutableListOf<Any>()

    /**
     * 批量添加中的添加实体。
     */
    fun addEntity(entity: IEsDocument) {
        if (entity.id.isEmpty()) {
            throw RuntimeException("批量更新时需要指定Id")
        }

        entity.updateAt = LocalDateTime.now()

        this.entities.add(entity)
    }

    fun addEntity(entity: JsonMap) {
        if (entity.getStringValue("id").isNullOrEmpty()) {
            throw RuntimeException("批量更新时需要指定Id")
        }

        entity.put("updateAt", LocalDateTime.now());

        this.entities.add(entity)
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

        var settingResult = db.es.esEvents.onUpdating(this)
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
        this.entities.forEach {
            var id = "";
            if (it is IEsDocument) {
                id = it.id;
            } else if (it is Map<*, *>) {
                id = it.get("id").AsString()
            }

            if (id.isNullOrEmpty()) {
                throw RuntimeException("更新实体缺少 id值")
            }

            data.add(JsonMap("update" to JsonMap("_index" to this.collectionName, "_id" to id)))

            data.add(JsonMap("doc" to it, "_source" to true))
        }

        var requestBody = "";
        usingScope(arrayOf(JsonStyleEnumScope.DateUtcStyle, JsonStyleEnumScope.Compress)) {
            requestBody = data.map { it.ToJson() + const.line_break }.joinToString("")
        }

        request.entity = NStringEntity(requestBody, ContentType.create("application/x-ndjson", const.utf8))


        var startAt = LocalDateTime.now()
        var response: Response? = null;
        var error: Exception? = null;
        try {
            response = esTemplate.performRequest(request)
            if (response.statusLine.statusCode != 200) {
                return ret;
            }

            db.executeTime = LocalDateTime.now() - startAt
//            responseBody = response.entity.content.readBytes().toString(const.utf8)

            usingScope(arrayOf(OrmLogScope.IgnoreAffectRow, OrmLogScope.IgnoreExecuteTime)) {
                settingResult.forEach {
                    it.first.update(this, it.second)
                }
            }

            ret = entities.size;
            db.affectRowCount = entities.size
            return db.affectRowCount
        } catch (e: Exception) {
            error = e;
            throw e;
        } finally {
            EsLogger.logPut(error, collectionName, request, response);
        }

        return ret;
    }
}
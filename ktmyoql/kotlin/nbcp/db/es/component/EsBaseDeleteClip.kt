package nbcp.db.es

import nbcp.comm.*
import nbcp.utils.*
import nbcp.db.db
import nbcp.db.es.*
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.elasticsearch.client.Request
import org.slf4j.LoggerFactory
import java.lang.RuntimeException
import java.time.LocalDateTime

open class EsBaseDeleteClip(tableName: String) : EsClipBase(tableName), IEsWhereable {
    companion object {
        private val logger by lazy {
            return@lazy LoggerFactory.getLogger(this::class.java)
        }
    }

    var routing = ""
    var pipeline = ""
    var refresh: EsPutRefreshEnum? = null
    var ids = mutableListOf<String>()

    /**
     * 批量添加中的添加实体。
     */
    fun addId(id:String) {
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
        var ret = 0;

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
        this.ids.forEach { id->
            if( id.isNullOrEmpty()){
                throw RuntimeException("更新实体缺少 id值")
            }

            data.add(JsonMap("delete" to JsonMap("_index" to this.collectionName, "_id" to id)))
        }

        var requestBody = "";
        usingScope(arrayOf(JsonStyleEnumScope.DateUtcStyle,JsonStyleEnumScope.Compress)) {
            requestBody = data.map { it.ToJson() + line_break }.joinToString("")
        }

        request.entity = NStringEntity(requestBody, ContentType.create("application/x-ndjson", utf8))

        var responseBody = "";
        var startAt = LocalDateTime.now()
        try {
            var response = esTemplate.performRequest(request)
            if (response.statusLine.statusCode != 200) {
                return ret;
            }

            db.executeTime = LocalDateTime.now() - startAt
            responseBody = response.entity.content.readBytes().toString(utf8)

            usingScope(arrayOf(OrmLogScope.IgnoreAffectRow, OrmLogScope.IgnoreExecuteTime)) {
                settingResult.forEach {
                    it.first.delete(this, it.second)
                }
            }

            ret = ids.size;
            db.affectRowCount = ids.size
            return db.affectRowCount
        } catch (e: Exception) {
            ret = -1;
            throw e;
        } finally {
            logger.InfoError(ret < 0) {
                """[insert] ${this.collectionName}
[url] ${request.method} ${request.endpoint}
${if (logger.debug) "[body] ${requestBody}" else "[enities.size] ${ids.size}"}
[result] ${if (logger.debug) responseBody else ret}
[耗时] ${db.executeTime}
"""
            };
        }

        return ret;
    }
}
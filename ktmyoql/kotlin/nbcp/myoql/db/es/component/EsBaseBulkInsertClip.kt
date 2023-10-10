package nbcp.myoql.db.es.component

import nbcp.base.comm.JsonMap
import nbcp.base.comm.config
import nbcp.base.comm.const
import nbcp.base.enums.JsonStyleScopeEnum
import nbcp.base.extend.*
import nbcp.myoql.db.db
import nbcp.myoql.db.enums.MyOqlDbScopeEnum
import nbcp.myoql.db.es.base.EsResultMsg
import nbcp.myoql.db.es.enums.EsPutRefreshEnum
import nbcp.myoql.db.es.extend.getResultMsg
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.elasticsearch.client.Request
import org.slf4j.LoggerFactory
import java.time.LocalDateTime


/**
 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-bulk.html
 */
open class EsBaseBulkInsertClip(tableName: String) : EsClipBase(tableName) {
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
    fun addEntity(entity: Any) {
        //id,createAt在拦截器中实现。
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
        var ret = 0;

        var settingResult = db.es.esEvents.onInserting(this)
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

        var request = Request("POST", this.collectionName + "/_bulk" +
            search.toUrlQuery().IfHasValue { "?" + it }
        )

        val data = mutableListOf<Any>()
        this.entities.forEach {
            var id = "";
            if (it is Map<*, *>) {
                id = it.get("id").AsString()
            } else {
                val idField = it.javaClass.FindField("id")
                if (idField != null) {
                    id = idField.get(it).AsString()
                }
            }
            data.add(JsonMap("create" to JsonMap("_index" to this.collectionName, "_id" to id)))

            data.add(it)
        }

        var requestBody = "";
        // 关于es中时间类型，仅支持 "yyyy-MM-dd"、"yyyyMMdd"、"yyyyMMddHHmmss"、"yyyy-MM-ddTHH:mm:ss"、"yyyy-MM-ddTHH:mm:ss.SSS"、"yyyy-MM-ddTHH:mm:ss.SSSZ"格式
        // https://www.cnblogs.com/koushr/p/9498888.html
        usingScope(arrayOf(JsonStyleScopeEnum.DATE_UTC_STYLE, JsonStyleScopeEnum.COMPRESS)) {
            requestBody = data.map { it.ToJson() + const.line_break }.joinToString("")
        }
        request.entity = NStringEntity(requestBody, ContentType.create("application/x-ndjson", const.utf8))

        logger.info(request.ToJson())
        var responseBody = EsResultMsg()
        val startAt = LocalDateTime.now()
        try {
            val response = esTemplate.performRequest(request)
            if (response.statusLine.statusCode != 200) {
                return ret;
            }

            db.executeTime = LocalDateTime.now() - startAt

            responseBody = response.getResultMsg();

            if (responseBody.error) {
                throw RuntimeException(responseBody.toString())
            }

            usingScope(arrayOf(MyOqlDbScopeEnum.IGNORE_AFFECT_ROW, MyOqlDbScopeEnum.IGNORE_EXECUTE_TIME)) {
                settingResult.forEach {
                    it.first.insert(this, it.second)
                }
            }

            ret = entities.size;
            db.affectRowCount = entities.size
            return ret
        } catch (e: Exception) {
            ret = -1;
            throw e;
        } finally {
            logger.InfoError(ret < 0) {
                """[insert] ${this.collectionName}
[url] ${request.method} ${request.endpoint}
${if (config.debug) "[body] ${requestBody}" else "[enities.size] ${entities.size}"}
[result] ${if (config.debug) responseBody.toString() else ret}
[耗时] ${db.executeTime}
"""
            };
        }

        return ret;
    }
}
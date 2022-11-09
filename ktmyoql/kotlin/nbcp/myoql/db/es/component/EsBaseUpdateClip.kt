package nbcp.myoql.db.es.component

import nbcp.base.comm.JsonMap
import nbcp.base.comm.const
import nbcp.base.enums.JsonStyleScopeEnum
import nbcp.base.extend.*
import nbcp.myoql.db.BaseEntity
import nbcp.myoql.db.db
import nbcp.myoql.db.enums.MyOqlDbScopeEnum
import nbcp.myoql.db.es.enums.EsPutRefreshEnum
import nbcp.myoql.db.es.logger.logPut
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.elasticsearch.client.Request
import org.elasticsearch.client.Response
import org.slf4j.LoggerFactory
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
    fun addEntity(entity: Any) {
        if (entity is BaseEntity) {
            if (entity.id.isEmpty()) {
                throw RuntimeException("批量更新时需要指定Id")
            }

            entity.updateAt = LocalDateTime.now();
        } else if (entity is MutableMap<*, *>) {
            var map = entity as MutableMap<String, Any?>
            if (map.get("id").AsString().isNullOrEmpty()) {
                throw RuntimeException("批量更新时需要指定Id")
            }
            map.set("updateAt", LocalDateTime.now())
        } else {
            //反射两个属性 id,createAt
            var entityClassFields = entity.javaClass.AllFields
            var idField = entityClassFields.firstOrNull { it.name == "id" }
            if (idField != null && idField.type.IsStringType) {
                var idValue = idField.get(entity).AsString();
                if (idValue.isEmpty()) {
                    throw RuntimeException("批量更新时需要指定Id")
                }
            }

            var updateAtField = entityClassFields.firstOrNull { it.name == "updateAt" }
            if (updateAtField != null) {
                updateAtField.set(entity, LocalDateTime.now())
            }
        }

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
            if (it is Map<*, *>) {
                id = it.get("id").AsString()
            } else {
                var idField = it.javaClass.FindField("id");
                if (idField != null) {
                    id = idField.get(it).AsString();
                }
            }

            if (id.isNullOrEmpty()) {
                throw RuntimeException("更新实体缺少 id值")
            }

            data.add(JsonMap("update" to JsonMap("_index" to this.collectionName, "_id" to id)))

            data.add(JsonMap("doc" to it, "_source" to true))
        }

        var requestBody = "";
        usingScope(arrayOf(JsonStyleScopeEnum.DateUtcStyle, JsonStyleScopeEnum.Compress)) {
            requestBody = data.map { it.ToJson() + const.line_break }.joinToString("")
        }

        request.entity = NStringEntity(requestBody, ContentType.create("application/x-ndjson", const.utf8))


        val startAt = LocalDateTime.now()
        var response: Response? = null;
        var error: Exception? = null;
        try {
            response = esTemplate.lowLevelClient.performRequest(request)
            if (response.statusLine.statusCode != 200) {
                return ret;
            }

            db.executeTime = LocalDateTime.now() - startAt
//            responseBody = response.entity.content.readBytes().toString(const.utf8)

            usingScope(arrayOf(MyOqlDbScopeEnum.IgnoreAffectRow, MyOqlDbScopeEnum.IgnoreExecuteTime)) {
                settingResult.forEach {
                    it.first.update(this, it.second)
                }
            }

//            ret = entities.size;
            db.affectRowCount = entities.size
            return db.affectRowCount
        } catch (e: Exception) {
            error = e;
            throw e;
        } finally {
            logger.logPut(
                error, collectionName, request,
                response?.statusLine?.statusCode.AsString() + "," + entities.size
            );
        }

        return ret;
    }
}
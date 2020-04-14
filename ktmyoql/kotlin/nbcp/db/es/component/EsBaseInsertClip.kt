package nbcp.db.es

import nbcp.comm.*
import nbcp.db.*
import nbcp.db.es.*
import nbcp.utils.CodeUtil
import org.bson.types.ObjectId
import org.elasticsearch.client.Request
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.time.LocalDateTime

open class EsBaseInsertClip(tableName: String) : EsClipBase(tableName), IEsWhereable {
    companion object {
        private val logger by lazy {
            return@lazy LoggerFactory.getLogger(this::class.java)
        }
    }

    var entities = mutableListOf<Any>()

    /**
     * 批量添加中的添加实体。
     */
    fun addEntity(entity: IEsDocument) {

        if (entity.id.isEmpty()) {
            entity.id = ObjectId().toString()
        }
        entity.createAt = LocalDateTime.now()

        this.entities.add(entity)
    }

    fun addEntity(entity: JsonMap) {
        if (entity.getStringValue("id").isEmpty()) {
            entity.put("id", CodeUtil.getCode())
        }

        entity.put("createAt", LocalDateTime.now());

        this.entities.add(entity)
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

        var request = Request("POST", "/_bulk")
        var data = mutableListOf<Any>()
        this.entities.forEach {
            var id = "";
            if (it is IEsDocument) {
                id = it.id;
            } else if (it is Map<*, *>) {
                id = it.get("id").AsString()
            }
            data.add(JsonMap("create" to JsonMap("_index" to this.collectionName, "_id" to id)))

            data.add(it)
        }

        request.setJsonEntity(data.ToJson())

        var responseBody = "";
        var startAt = LocalDateTime.now()
        try {
            var response = esTemplate.performRequest(request)
            if (response.statusLine.statusCode != 200) {
                return ret;
            }

            db.executeTime = LocalDateTime.now() - startAt
            responseBody = response.entity.content.readBytes().toString(utf8)

            using(OrmLogScope.IgnoreAffectRow) {
                using(OrmLogScope.IgnoreExecuteTime) {
                    settingResult.forEach {
                        it.first.insert(this, it.second)
                    }
                }
            }

            ret = entities.size;
            db.affectRowCount = entities.size
            return db.affectRowCount
        } catch (e: Exception) {
            ret = -1;
            throw e;
        } finally {
            logger.InfoError(ret < 0) {
                """[insert] ${this.collectionName}
[url] ${request.method} ${request.endpoint}
${if (db.debug) "[enities.size] ${entities.size}" else "[entities] ${entities.ToJson()}"}
[result] ${if (db.debug) responseBody else ret}
[耗时] ${db.executeTime}
"""
            };
        }

        return ret;
    }
}
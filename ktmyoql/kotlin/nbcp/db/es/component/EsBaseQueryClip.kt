package nbcp.db.es

import nbcp.comm.*
import nbcp.utils.*
import nbcp.db.db
import org.apache.http.message.BasicHeader
import org.bson.Document
import org.elasticsearch.client.Request
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.time.LocalDateTime

open class EsBaseQueryClip(tableName: String) : EsClipBase(tableName), IEsWhereable {

    var routing = "";
    var search = SearchBodyClip()

    fun selectField(column: String) {
        search._source.add(column);
    }

    fun withRouting(routing: String = "") {
        this.routing = routing;
    }

    companion object {
        private val logger by lazy {
            return@lazy LoggerFactory.getLogger(this::class.java)
        }
    }


    /**
     * 返回该对象的 Md5。
     */
    private fun getCacheKey(): String {
        var unKeys = mutableListOf<String>()

        unKeys.add(search.toString())

        return Md5Util.getBase64Md5(unKeys.joinToString("\n"));
    }

    var total: Int = -1;

    /**
     * 核心功能，查询列表，原始数据对象是 Document
     */
    fun <R> toList(clazz: Class<R>, mapFunc: ((Document) -> Unit)? = null): MutableList<R> {
        db.affectRowCount = 0;
        var isString = clazz.IsStringType();

        var search = JsonMap();
        if (this.routing.HasValue) {
            search.put("routing", this.routing)
        }


        var url = "/${collectionName}/_search" + search.toUrlQuery().IfHasValue { "?" + it }
        var request = Request("POST", url);

        var requestBody = ""
        using(arrayOf(JsonStyleEnumScope.DateUtcStyle,JsonStyleEnumScope.Compress)) {
            requestBody = this.search.toString()
        }
//        request.options.headers.add(BasicHeader("content-type", "application/x-ndjson"));
        request.setJsonEntity(requestBody)

        var responseBody = "";
        var error = false;
        var startAt = LocalDateTime.now();
        var list: List<Map<String, Any>> = listOf()

        var ret = mutableListOf<R>();
        try {
            var response = esTemplate.performRequest(request)
            db.executeTime = LocalDateTime.now() - startAt

            if (response.statusLine.statusCode != 200) {
                return ret;
            }
            responseBody = response.entity.content.readBytes().toString(utf8)

            var lastKey = this.search._source.lastOrNull() ?: ""
            var result = responseBody.FromJson<Map<String, Any>>()!!;

            this.total = result.getIntValue("_shards", "total")
            if (this.total <= 0) {
                return ret;
            }

            var hits = result.get("hits") as Map<String, Any>
            list = (hits.get("hits") as List<*>)
                    .map { (it as Map<String, Any>).get("_source") as Map<String, Any> };

            db.affectRowCount = list.size

            list.forEach {
                if (isString) {
                    if (lastKey.isEmpty()) {
                        lastKey = it.keys.last()
                    }

                    ret.add(it.getPathValue(*lastKey.split(".").toTypedArray()).AsString() as R)
                } else if (clazz.IsSimpleType()) {
                    if (lastKey.isEmpty()) {
                        lastKey = it.keys.last()
                    }

                    ret.add(it.getPathValue(*lastKey.split(".").toTypedArray()) as R);
                } else {
                    var ent = it.ConvertJson(clazz)
                    ret.add(ent);
                }
            }

        } catch (e: Exception) {
            error = true;
            throw e;
        } finally {
            fun getMsgs(): String {
                var msgs = mutableListOf<String>()
                msgs.add("[index] " + this.collectionName);
                msgs.add("[url] " + url);
                msgs.add("[search] " + requestBody)

                if (logger.debug) {
                    msgs.add("[result] ${responseBody}")
                } else {
                    msgs.add("[result.size] " + list.size.toString())
                }

                msgs.add("[耗时] ${db.executeTime}")
                return msgs.joinToString(line_break);
            }

            logger.InfoError(error) { getMsgs() }
        }

        return ret
    }


    fun <R> toListResult(clazz: Class<R>, mapFunc: ((Document) -> Unit)? = null): ListResult<R> {
        var ret = ListResult<R>();
        ret.data = toList(clazz, mapFunc);
        ret.total = this.total;
        return ret;
    }


    fun toMapListResult(): ListResult<JsonMap> {
        return toListResult(JsonMap::class.java);
    }
}
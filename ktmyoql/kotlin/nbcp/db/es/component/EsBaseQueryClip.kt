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

    @JvmOverloads
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

    private fun getRestResult(url: String, requestBody: String): String {
        db.affectRowCount = 0;

        var request = Request("POST", url);
        request.setJsonEntity(requestBody)

        var startAt = LocalDateTime.now();
        try {
            var response = esTemplate.performRequest(request)
            db.executeTime = LocalDateTime.now() - startAt

            if (response.statusLine.statusCode != 200) {
                return "";
            }
            return response.entity.content.readBytes().toString(utf8)

        } catch (e: Exception) {
            throw e;
        }
    }

    /**
     * 核心功能，查询列表，原始数据对象是 Document
     */
    @JvmOverloads
    fun <R> toList(clazz: Class<R>, mapFunc: ((Map<String, Any?>) -> Unit)? = null): MutableList<R> {
        var settingResult = db.es.esEvents.onQuering(this)
        if (settingResult.any { it.second.result == false }) {
            return mutableListOf();
        }


        var isString = clazz.IsStringType;

        var error = false;
        var list: List<Map<String, Any>> = listOf()

        var ret = mutableListOf<R>();
        var responseBody = "";
        var url = getUrl()

        var requestBody = ""
        usingScope(arrayOf(JsonStyleEnumScope.DateUtcStyle, JsonStyleEnumScope.Compress)) {
            requestBody = this.search.toString()
        }

        try {
            responseBody = getRestResult(url, requestBody)

            var result = responseBody.FromJson<Map<String, Any>>()!!;

            var hits = result.getTypeValue<Map<String, *>>("hits");
            if (hits == null) {
                return ret;
            }

            this.total = hits.getIntValue("total", "value");
            if (this.total <= 0) {
                return ret;
            }

            list = (hits.getTypeValue<Collection<*>>("hits") ?: listOf<Any>())
                .map { (it as Map<String, *>).getTypeValue<Map<String, Any>>("_source") }
                .filter { it != null }
                .map { it!! }


            db.affectRowCount = list.size

            var lastKey = this.search._source.lastOrNull() ?: ""

            list.forEach {
                if (mapFunc != null) {
                    mapFunc(it);
                }


                if (isString) {
                    if (lastKey.isEmpty()) {
                        lastKey = it.keys.last()
                    }

                    ret.add(MyUtil.getPathValue(it, *lastKey.split(".").toTypedArray()).AsString() as R)
                } else if (clazz.IsSimpleType()) {
                    if (lastKey.isEmpty()) {
                        lastKey = it.keys.last()
                    }

                    ret.add(MyUtil.getPathValue(it, *lastKey.split(".").toTypedArray()) as R);
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


    fun <R> toListResult(clazz: Class<R>, mapFunc: ((Map<String, Any?>) -> Unit)? = null): ListResult<R> {
        var ret = ListResult<R>();
        ret.data = toList(clazz, mapFunc);
        ret.total = this.total;
        return ret;
    }


    fun toMapListResult(): ListResult<JsonMap> {
        return toListResult(JsonMap::class.java);
    }

    /**
     * 获取总条数
     * https://www.elastic.co/guide/en/elasticsearch/reference/7.6/search-count.html
     */
    fun count(): Int {
        var error = false;
        var count = 0;
        var responseBody = "";
        var url = getUrl()

        var requestBody = ""
        usingScope(arrayOf(JsonStyleEnumScope.DateUtcStyle, JsonStyleEnumScope.Compress)) {
            requestBody = this.search.toString()
        }

        try {
            responseBody = getRestResult(url, requestBody)

            var result = responseBody.FromJson<Map<String, Any>>()!!;

            count = result.getIntValue("count")
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
                    msgs.add("[count] " + count)
                }

                msgs.add("[耗时] ${db.executeTime}")
                return msgs.joinToString(line_break);
            }

            logger.InfoError(error) { getMsgs() }
        }

        return count
    }

    /**
     * 获取 aggregations 部分
     */
    fun getAggregationResult(): Map<String, *> {
        var error = false;
        var ret = JsonMap()
        var responseBody = "";
        var url = getUrl()

        var requestBody = ""
        usingScope(arrayOf(JsonStyleEnumScope.DateUtcStyle, JsonStyleEnumScope.Compress)) {
            requestBody = this.search.toString()
        }

        try {
            responseBody = getRestResult(url, requestBody)

            var result = responseBody.FromJson<Map<String, Any>>()!!;

            var hits = result.getTypeValue<Map<String, *>>("hits");
            if (hits == null) {
                return ret;
            }

            this.total = hits.getIntValue("total", "value");
            if (this.total <= 0) {
                return ret;
            }

            return result.getTypeValue<Map<String, *>>("aggregations") ?: JsonMap()
        } catch (e: Exception) {
            error = true;
            throw e;
        } finally {
            fun getMsgs(): String {
                var msgs = mutableListOf<String>()
                msgs.add("[index] " + this.collectionName);
                msgs.add("[url] " + url);
                msgs.add("[search] " + requestBody)


                msgs.add("[result] ${responseBody}")


                msgs.add("[耗时] ${db.executeTime}")
                return msgs.joinToString(line_break);
            }

            logger.InfoError(error) { getMsgs() }
        }

        return ret
    }

    private fun getUrl(): String {
        var search = JsonMap();
        if (this.routing.HasValue) {
            search.put("routing", this.routing)
        }


        var url = "/${collectionName}/_search" + search.toUrlQuery().IfHasValue { "?" + it }
        return url
    }
}
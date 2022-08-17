package nbcp.db.es

import nbcp.comm.*
import nbcp.utils.*
import nbcp.db.db
import nbcp.scope.*
import org.elasticsearch.client.Request
import org.elasticsearch.client.Response
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
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
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

    private fun getRestResult(url: String, requestBody: String): Map<String, Any?> {
        db.affectRowCount = 0;

        val request = Request("POST", url);
        request.setJsonEntity(requestBody)
        var response: Response? = null
        val startAt = LocalDateTime.now();
        var error: Exception? = null
        var responseData: Map<String, Any?>? = null
        try {
            response = esTemplate.lowLevelClient.performRequest(request)
            db.executeTime = LocalDateTime.now() - startAt

            if (response.statusLine.statusCode != 200) {
                return mapOf()
            }

            responseData = response.entity.content
                .readContentString()
                .FromJson<Map<String, Any?>>() ?: mapOf();
            return responseData;
        } catch (e: Exception) {
            error = e;
            throw e;
        } finally {
            EsLogger.logGet(
                error,
                collectionName,
                request,
                response?.statusLine?.statusCode.AsString() + "," +
                    responseData?.getStringValue("hits.total.value").AsString()
            )
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
//        var isString = clazz.IsStringType;

        var list: List<Map<String, Any>> = listOf()

        var ret = mutableListOf<R>();
        var url = getUrl("_search")

        var requestBody = ""
        usingScope(arrayOf(JsonStyleEnumScope.DateUtcStyle, JsonStyleEnumScope.Compress)) {
            requestBody = this.search.toString()
        }

        var result = getRestResult(url, requestBody)

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

        var skipNullCount = 0;
        list.forEach {
            if (mapFunc != null) {
                mapFunc(it);
            }


//            if (isString) {
//                if (lastKey.isEmpty()) {
//                    lastKey = it.keys.last()
//                }
//
//                ret.add(MyUtil.getValueByWbsPath(it, *lastKey.split(".").toTypedArray()).AsString() as R)
//            } else
            if (clazz.IsSimpleType()) {
                if (lastKey.isEmpty()) {
                    lastKey = it.keys.last()
                }

                var value = MyUtil.getValueByWbsPath(it, *lastKey.split(".").toTypedArray())
                if (value != null) {
                    ret.add(value.ConvertType(clazz) as R);
                } else {
                    skipNullCount++;
                }
            } else {
                var ent = it.ConvertJson(clazz)
                ret.add(ent);
            }
        }


        if (skipNullCount > 0) {
            logger.warn("skipNullRows:${skipNullCount}")
        }

        return ret
    }

    @JvmOverloads
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
        var count = 0;
        var url = getUrl("_count")

        var requestBody = ""
        usingScope(arrayOf(JsonStyleEnumScope.DateUtcStyle, JsonStyleEnumScope.Compress)) {
            requestBody = this.search.toString()
        }

        var result = getRestResult(url, requestBody)

        count = result.getIntValue("count")


        return count
    }

    /**
     * 获取 aggregations 部分
     */
    fun getAggregationResult(): Map<String, *> {
        var ret = JsonMap()
        var url = getUrl("_search")

        var requestBody = ""
        usingScope(arrayOf(JsonStyleEnumScope.DateUtcStyle, JsonStyleEnumScope.Compress)) {
            requestBody = this.search.toString()
        }

        var result = getRestResult(url, requestBody)

        var hits = result.getTypeValue<Map<String, *>>("hits");
        if (hits == null) {
            return ret;
        }

        this.total = hits.getIntValue("total", "value");
        if (this.total <= 0) {
            return ret;
        }

        return result.getTypeValue<Map<String, *>>("aggregations") ?: JsonMap()
    }

    private fun getUrl(action:String): String {
        var search = JsonMap();
        if (this.routing.HasValue) {
            search.put("routing", this.routing)
        }


        val url = "/${collectionName}/${action}" + search.toUrlQuery().IfHasValue { "?" + it }
        return url
    }
}
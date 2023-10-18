package nbcp.myoql.db.es.component

import nbcp.base.comm.JsonMap
import nbcp.base.comm.ListResult
import nbcp.base.enums.JsonStyleScopeEnum
import nbcp.base.extend.*
import nbcp.base.extend.*
import nbcp.base.extend.*
import nbcp.base.extend.*
import nbcp.base.utils.Md5Util
import nbcp.base.utils.ReflectUtil
import nbcp.myoql.db.db
import nbcp.myoql.db.es.base.EsColumnName
import nbcp.myoql.db.es.logger.logGet
import org.elasticsearch.client.Request
import org.elasticsearch.client.Response
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

open class EsBaseQueryClip(tableName: String) : EsClipBase(tableName), IEsWhereable {

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }
    var routing = "";
    var search = SearchBodyClip()

    /**
     * 批量添加中的添加实体。
     */
    override fun whereId(id: String) {
        if (id.isEmpty()) {
            throw RuntimeException("批量删除时需要指定Id")
        }

        this.search.ids.add(id)
    }

    fun selectField(column: String) {
        search._source.add(column);
    }

    @JvmOverloads
    fun withRouting(routing: String = "") {
        this.routing = routing;
    }



    /**
     * 返回该对象的 Md5。
     */
    private fun getCacheKey(): String {
        var unKeys = mutableListOf<String>()

        unKeys.add(search.toString())

        return Md5Util.getBase64Md5(unKeys.joinToString("\n"));
    }



    fun setLimit(skip: Long, take: Int)  {
        this.search.skip = skip;
        this.search.take = take;
    }

    /**
     * 升序
     */
    fun setOrderByAsc(sort : EsColumnName)  {
          this.orderBy(true, sort)
    }

    /**
     * 降序
     */
    fun setOrderByDesc(sort :   EsColumnName)  {
          this.orderBy(false, sort)
    }

    private fun orderBy(asc: Boolean, column: EsColumnName)  {
        var order_str = "";
        if (asc) {
            order_str = "asc"
        } else {
            order_str = "desc"
        }

        this.search.sort.add(JsonMap(column.toString() to JsonMap("order" to order_str)))
    }

    fun setShould( vararg where:   WhereData) {
        this.search.query.addShould(*where)
    }

    fun setMust(vararg  where:  WhereData)  {
        this.search.query.addMust(* where )
    }

    fun setMustNot( vararg where:  WhereData)  {
        this.search.query.addMustNot( *where )
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

        logger.info(request.ToJson())

        try {
            response = esTemplate.performRequest(request)
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
            logger.logGet(
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
    fun <R> toList(type: Class<R>, mapFunc: ((Map<String, Any?>) -> Unit)? = null): MutableList<R> {
        var settingResult = db.es.esEvents.onQuering(this)
        if (settingResult.any { it.second.result == false }) {
            return mutableListOf();
        }
//        var isString = clazz.IsStringType;

        var list: List<Map<String, Any?>> = listOf()

        var ret = mutableListOf<R>();
        var url = getUrl("_search")

        var requestBody = ""
        usingScope(arrayOf(JsonStyleScopeEnum.DATE_UTC_STYLE, JsonStyleScopeEnum.COMPRESS)) {
            requestBody = this.search.toString()
        }

        var result = getRestResult(url, requestBody)

        var hits = result.getTypeValue<Map<String, *>>("hits");
        if (hits == null) {
            return ret;
        }

        db.affectRowCount = 0;

        this.total = hits.getIntValue("total", "value");
        if (this.total <= 0) {
            return ret;
        }

        var hitResult = hits.getTypeValue<Collection<*>>("hits");
        if (hitResult.isNullOrEmpty()) {
            return ret;
        }

        list = hitResult
            .map {
                var m = it as Map<String, *>;
                var ret = m.getTypeValue<MutableMap<String, Any?>>("_source")!!
                if (ret.containsKey("_id") == false) {
                    ret.put("_id", m.get("_id"))
                }
                return@map ret
            }


        db.affectRowCount = list.size

        var lastKey = this.search._source.lastOrNull() ?: ""

        var skipNullCount = 0;
        list.forEach {
            if (mapFunc != null) {
                mapFunc(it);
            }

            if (type.IsSimpleType()) {
                if (lastKey.isEmpty()) {
                    lastKey = it.keys.last()
                }

                var value = ReflectUtil.getValueByWbsPath(it, *lastKey.split(".").toTypedArray())
                if (value != null) {
                    ret.add(value.ConvertType(type) as R);
                } else {
                    skipNullCount++;
                }
            } else {
                var ent = it.ConvertJson(type)
                ret.add(ent);
            }
        }

        if (skipNullCount > 0) {
            logger.warn("skipNullRows:${skipNullCount}")
        }

        return ret
    }

    @JvmOverloads
    fun <R> toListResult(
        type: Class<R>,
        mapFunc: ((Map<String, Any?>) -> Unit)? = null
    ): ListResult<R> {
        var ret = ListResult<R>();
        ret.data = toList(type, mapFunc);
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
        usingScope(arrayOf(JsonStyleScopeEnum.DATE_UTC_STYLE, JsonStyleScopeEnum.COMPRESS)) {
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
        usingScope(arrayOf(JsonStyleScopeEnum.DATE_UTC_STYLE, JsonStyleScopeEnum.COMPRESS)) {
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

    private fun getUrl(action: String): String {
        var search = JsonMap();
        if (this.routing.HasValue) {
            search.put("routing", this.routing)
        }


        val url = "/${collectionName}/${action}" + search.toUrlQuery().IfHasValue { "?" + it }
        return url
    }
}
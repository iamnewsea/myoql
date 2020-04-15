package nbcp.db.es

import nbcp.comm.*
import nbcp.utils.*
import nbcp.db.db
import org.bson.Document
import org.elasticsearch.client.Request
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.time.LocalDateTime

open class EsBaseQueryClip(tableName: String) : EsClipBase(tableName), IEsWhereable {

    var search = SearchBodyClip()

    fun selectField(column: String) {
        search._source.add(column);
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

    var total:Int = -1;

    /**
     * 核心功能，查询列表，原始数据对象是 Document
     */
    fun <R> toList(clazz: Class<R>, mapFunc: ((Document) -> Unit)? = null): MutableList<R> {
        db.affectRowCount = 0;
        var isString = clazz.IsStringType();

        var request = Request("POST", "/${collectionName}/_search")
        request.setJsonEntity(this.search.toString())

        var startAt = LocalDateTime.now();
        var response = esTemplate.performRequest(request)
        db.executeTime = LocalDateTime.now() - startAt

        var ret = mutableListOf<R>();
        if (response.statusLine.statusCode != 200) {
            return ret;
        }
        var responseBody = response.entity.content.readBytes().toString(utf8)
        var result = responseBody.FromJson<Map<String,Any>>()!!;
        var hits = result.get("hits") as Map<String,Any>
        this.total = result.getIntValue("_shards","total")
        if( this.total <=0 ){
            return ret;
        }


        var list = (hits.get("hits") as List<*>).map{ (it as Map<String,Any>).get("_source") as Map<String,Any> };

        db.affectRowCount = list.size

        var lastKey = this.search._source.lastOrNull() ?: ""
        var error = false;
        try {
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
                msgs.add("[search] " + this.search.toString())

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
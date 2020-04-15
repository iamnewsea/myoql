package nbcp.db.es

import nbcp.comm.*
import nbcp.utils.*
import nbcp.db.db
import nbcp.db.es.*
import org.elasticsearch.client.Request
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

open class EsBaseUpdateClip(tableName: String) : EsClipBase(tableName), IEsWhereable {
    companion object {
        private val logger by lazy {
            return@lazy LoggerFactory.getLogger(this::class.java)
        }
    }

    var search = SearchBodyClip()


    /**
     * 更新条件不能为空。
     */
    open fun exec(): Int {
        if (search.isEmpty()) {
            throw RuntimeException("更新条件为空，不允许更新")
            return 0;
        }

        return execAll();
    }


    /**
     * 更新条件可以为空。
     */
    protected fun execAll(): Int {
        db.affectRowCount = -1;


        var settingResult = db.es.esEvents.onUpdating(this)
        if (settingResult.any { it.second.result == false }) {
            return 0;
        }

        var request = Request("POST", "/${collectionName}/_search")
        request.setJsonEntity(this.search.toString())
        var ret = 0;
        var startAt = LocalDateTime.now()
        var responseBody = "";
        try {
            var response = esTemplate.performRequest(request)

            db.executeTime = LocalDateTime.now() - startAt


            if (response.statusLine.statusCode != 200) {
                return 0;
            }
            responseBody = response.entity.content.readBytes().toString(utf8)
//            var result = responseBody.FromJson<JsonMap>()!!;

            ret = 0
            db.affectRowCount = ret
        } catch (e: Exception) {
            ret = -1;
            throw e;
        } finally {
            logger.InfoError(ret < 0) {
                """[index] ${this.collectionName}
[url] ${request.method} ${request.endpoint} 
[body] ${search} 
[result] ${if (logger.debug) responseBody else ret}
[耗时] ${db.executeTime}"""
            }
        }


        return ret;
    }

}
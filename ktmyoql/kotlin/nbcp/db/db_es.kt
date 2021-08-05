package nbcp.db


import nbcp.comm.*
import nbcp.utils.*
import nbcp.db.es.*
import org.elasticsearch.client.*
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

/**
 * 请使用 db.mongo
 */
object db_es {
    private  val logger = LoggerFactory.getLogger(this::class.java)

    //所有的组。
    val groups = mutableSetOf<IDataGroup>()

    //     val sqlEvents = SpringUtil.getBean<SqlEventConfig>();
    val esEvents by lazy {
        return@lazy SpringUtil.getBean<EsEntityCollector>();
    }

    val restClient: RestClient by lazy {
        return@lazy SpringUtil.getBean<RestClient>()
    }

    fun createPipeline(name: String, description: String, vararg processors: JsonMap) {
        var request = Request("PUT", "/_ingest/pipeline/${name}")

        var json = JsonMap("description" to description, "processors" to processors)
        var requestBody = "";
        usingScope(arrayOf(JsonStyleEnumScope.DateUtcStyle,JsonStyleEnumScope.Compress)) {
            requestBody = json.ToJson()
        }

        request.setJsonEntity(requestBody)
        var error = false;
        var startAt = LocalDateTime.now()
        var responseBody = "";
        try {
            var response = restClient.performRequest(request)

            db.executeTime = LocalDateTime.now() - startAt


            if (response.statusLine.statusCode != 200) {
                return;
            }
            responseBody = response.entity.content.readBytes().toString(utf8)
//            var result = responseBody.FromJson<JsonMap>()!!;

        } catch (e: Exception) {
            error = true;
            throw e;
        } finally {
            logger.InfoError(error) {
                """[url] ${request.method} ${request.endpoint} 
[body] ${requestBody} 
[result] ${responseBody}
[耗时] ${db.executeTime}"""
            }
        }
    }
}
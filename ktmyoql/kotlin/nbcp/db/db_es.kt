package nbcp.db

import nbcp.scope.*
import nbcp.comm.*
import nbcp.utils.*
import nbcp.db.es.*
import org.apache.http.HttpHost
import org.elasticsearch.client.*
import org.slf4j.LoggerFactory
import java.lang.RuntimeException
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 请使用 db.mongo
 */
object db_es {
    private val logger = LoggerFactory.getLogger(this::class.java)

    //所有的组。
    val groups = mutableSetOf<IDataGroup>()

    //     val sqlEvents = SpringUtil.getBean<SqlEventConfig>();
    val esEvents by lazy {
        return@lazy SpringUtil.getBean<EsEntityCollector>();
    }

    fun proc_es_value(value: Any?): Any? {
        if (value == null) {
            return null;
        }

        var type = value::class.java
        if (type.isEnum) {
            return value.toString();
        } else if (type == LocalDateTime::class.java ||
            type == LocalDate::class.java
        ) {
            return value.AsLocalDateTime().AsDate()
        }

        return value;
    }

    fun multi_match(vararg target: Any?): WhereData {
        var target = proc_es_value(target);

        return WhereData("match" to JsonMap(
            this.toString() to target
        ))
    }

    private val restClientMap = mutableMapOf<String, RestClient>()

    @JvmStatic
    @JvmOverloads
    fun getRestClient(uri: String, pathPrefix: String = "", timeout: Int = 0): RestClient {

        var key = "${uri}-${pathPrefix}-${timeout}"
        var retValue = restClientMap.get(key);
        if (retValue != null) {
            return retValue;
        }

        var configs = uri
            .split(",")
            .map { it.trim() }
            .filter { it.HasValue }
            .map {
                var sect = it.split(":")
                if (sect[1].startsWith("//") == false) {
                    throw RuntimeException("spring.elasticsearch.rest.uris 格式错误")
                }

                var protocal = sect[0]
                var ip = sect[1].substring(2)
                var port = if (sect.size >= 2) sect[2].AsInt() else 9200


                return@map HttpHost(ip, port, protocal);
            }

        if (configs.isEmpty()) {
            throw RuntimeException("spring.elasticsearch.rest.uris 定义错误")
        }

        //配置可选参数
        val builder = RestClient.builder(*configs.toTypedArray())

//        val defaultHeaders = arrayOf<Header>(BasicHeader("header", "value"))
//        builder.setDefaultHeaders(defaultHeaders)


        if (pathPrefix.HasValue) {
            builder.setPathPrefix(pathPrefix)
        }

        builder.setFailureListener(object : RestClient.FailureListener() {
            fun onFailure(host: HttpHost?) {
                //设置一个监听程序，每次节点发生故障时都会收到通知，这样就可以采取相应的措施。
                //Used internally when sniffing on failure is enabled.(这句话没搞懂啥意思)
                if (host == null) return

                throw RuntimeException(host.toHostString() + "es 连接失败")
            }
        });

        if (timeout > 0) {
            builder.setRequestConfigCallback { requestConfigBuilder ->
                //设置允许修改默认请求配置的回调
                // （例如，请求超时，身份验证或org.apache.http.client.config.RequestConfig.Builder允许设置的任何内容）
                requestConfigBuilder.setSocketTimeout(timeout)
            }
        }
//        builder.setHttpClientConfigCallback { httpClientBuilder ->
//            //设置允许修改http客户端配置的回调
//            // （例如，通过SSL的加密通信，或者org.apache.http.impl.nio.client.HttpAsyncClientBuilder允许设置的任何内容）
//            httpClientBuilder.setProxy(HttpHost("proxy", 9000, "http"))
//        }

        retValue = builder.build();

        restClientMap.put(key, retValue);
        return retValue;
    }

    fun createPipeline(name: String, description: String, vararg processors: JsonMap) {
        var request = Request("PUT", "/_ingest/pipeline/${name}")

        var json = JsonMap("description" to description, "processors" to processors)
        var requestBody = "";
        usingScope(arrayOf(JsonStyleEnumScope.DateUtcStyle, JsonStyleEnumScope.Compress)) {
            requestBody = json.ToJson()
        }

        request.setJsonEntity(requestBody)

        var startAt = LocalDateTime.now()
        var error: Exception? = null;
        var response: Response? = null;

        try {
            response = SpringUtil.getBean<RestClient>().performRequest(request)

            db.executeTime = LocalDateTime.now() - startAt

            if (response.statusLine.statusCode != 200) {
                return;
            }
//            responseBody = response.entity.content.readBytes().toString(const.utf8)
//            var result = responseBody.FromJson<JsonMap>()!!;

        } catch (e: Exception) {
            error = e;
            throw e;
        } finally {
            EsLogger.logGet(error,name, request, response);
        }
    }
}
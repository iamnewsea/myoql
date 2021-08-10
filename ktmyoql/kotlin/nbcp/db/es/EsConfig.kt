package nbcp.db.es.tool

import nbcp.comm.*
import nbcp.utils.*
import org.apache.http.Header
import org.apache.http.HttpHost
import org.apache.http.message.BasicHeader
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestClient.FailureListener
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.Lazy
import java.lang.RuntimeException


@Configuration
@ConditionalOnProperty("spring.elasticsearch.rest.uris")
@ConditionalOnBean(ElasticsearchDataAutoConfiguration::class)
@Lazy
class MyOqlEsConfig {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    @Bean
    @Lazy
    fun myoqlEsDataSource(): RestClient {
        var configs = config.getConfig("spring.elasticsearch.rest.uris","")
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

        var pathPrefix = config.getConfig("spring.elasticsearch.path-prefix")
        if (pathPrefix.HasValue) {
            builder.setPathPrefix(pathPrefix)
        }

        builder.setFailureListener(object : FailureListener() {
            fun onFailure(host: HttpHost?) {
                //设置一个监听程序，每次节点发生故障时都会收到通知，这样就可以采取相应的措施。
                //Used internally when sniffing on failure is enabled.(这句话没搞懂啥意思)
                if (host == null) return

                logger.error(host.toHostString())
            }
        });

        var timeout = config.getConfig("spring.elasticsearch.timeout").AsInt()
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

        return builder.build()
    }
}
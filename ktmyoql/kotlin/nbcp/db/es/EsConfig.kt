package nbcp.db.es.tool

import nbcp.comm.*
import nbcp.db.AbstractMultipleDataSourceProperties
import nbcp.db.db
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
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.*
import org.springframework.stereotype.Component
import java.lang.RuntimeException


@Configuration
@ConditionalOnProperty("spring.elasticsearch.rest.uris")
@Import(SpringUtil::class)
@Lazy
class MyOqlEsConfig {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    @Bean
    @Lazy
    fun myoqlEsDataSource(): RestClient {
        return db.es.getRestClient(
            config.getConfig("spring.elasticsearch.rest.uris", ""),
            config.getConfig("spring.elasticsearch.path-prefix", ""),
            config.getConfig("spring.elasticsearch.timeout").AsInt()
        )
    }
}

/**
 * 定义Es不同的数据源
 */
@ConfigurationProperties(prefix = "app.es")
@Component
class EsIndexDataSource : AbstractMultipleDataSourceProperties() {
}
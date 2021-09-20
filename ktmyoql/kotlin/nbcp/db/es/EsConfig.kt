package nbcp.db.es.tool

import nbcp.comm.*
import nbcp.db.*
import nbcp.utils.*
import org.apache.http.Header
import org.apache.http.HttpHost
import org.apache.http.message.BasicHeader
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestClient.FailureListener
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.*
import org.springframework.stereotype.Component
import java.lang.RuntimeException


//@Configuration
//@ConditionalOnProperty("spring.elasticsearch.rest.uris")
//@Import(SpringUtil::class)
//@Lazy
//class MyOqlEsConfig {
//    companion object {
//        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
//    }
//
//    @Bean
//    @Lazy
//    fun myoqlEsDataSource(): RestClient {
//        return db.es.getRestClient(
//            config.getConfig("spring.elasticsearch.rest.uris", ""),
//            config.getConfig("spring.elasticsearch.path-prefix", ""),
//            config.getConfig("spring.elasticsearch.timeout").AsInt()
//        )
//    }
//}

/**
 * 定义Es不同的数据源
 */
@ConfigurationProperties(prefix = "app.es.ds")
@Component
class EsIndexDataSource : AbstractMyOqlMultipleDataSourceProperties() {
}


@ConfigurationProperties(prefix = "app.es.log")
@Component
class EsTableLogProperties : InitializingBean {
    var get: List<String> = listOf()
    var post: List<String> = listOf()
    var put: List<String> = listOf()
    var delete: List<String> = listOf()


    fun getGetLog(tableDbName: String): Boolean {
        if (get.contains(tableDbName.toLowerCase())) return true;
        if (logDefault.contains(HttpCrudEnum.get)) return true;
        return false;
    }

    fun getPostLog(tableDbName: String): Boolean {
        if (post.contains(tableDbName.toLowerCase())) return true;
        if (logDefault.contains(HttpCrudEnum.post)) return true;
        return false
    }

    fun getPutLog(tableDbName: String): Boolean {
        if (put.contains(tableDbName.toLowerCase())) return true;
        if (logDefault.contains(HttpCrudEnum.put)) return true;
        return false;
    }

    fun getDeleteLog(tableDbName: String): Boolean {
        if (delete.contains(tableDbName.toLowerCase())) return true;
        if (logDefault.contains(HttpCrudEnum.delete)) return true;
        return false;
    }


    val logDefault by lazy {
        var value = config.getConfig("app.es.log-default").AsString().trim();
        if (value.HasValue) {
            if (value == "*") {
                return@lazy HttpCrudEnum::class.java.GetEnumList()
            }
            return@lazy HttpCrudEnum::class.java.GetEnumList(value)
        }
        return@lazy listOf<HttpCrudEnum>()
    }

    override fun afterPropertiesSet() {
        get = get.map { it.toLowerCase() }
        post = post.map { it.toLowerCase() }
        put = put.map { it.toLowerCase() }
        delete = delete.map { it.toLowerCase() }
    }
}
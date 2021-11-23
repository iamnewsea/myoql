package nbcp.db.es.tool

import nbcp.comm.*
import nbcp.db.*
import nbcp.utils.*
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.*
import org.springframework.stereotype.Component


//@Component
//@ConditionalOnClass(RestClient::class)
//@ConditionalOnProperty("spring.elasticsearch.rest.uris")
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
class EsIndexDataSource : MyOqlMultipleDataSourceDefine() {
}


@ConfigurationProperties(prefix = "app.es.log")
@Component
class EsTableLogProperties : MyOqlBaseActionLogDefine("app.es.log-default") {
}
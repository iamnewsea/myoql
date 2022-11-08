package nbcp.myoql.db.es

import nbcp.base.comm.*
import nbcp.myoql.db.*
import nbcp.base.utils.*
import nbcp.myoql.db.comm.MyOqlBaseActionLogDefine
import nbcp.myoql.db.comm.MyOqlMultipleDataSourceDefine
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
@Component
class EsIndexDataSource : MyOqlMultipleDataSourceDefine("app.es") {
}


@ConfigurationProperties(prefix = "app.es.log")
@Component
class EsTableLogProperties : MyOqlBaseActionLogDefine("app.es.log-default") {
}
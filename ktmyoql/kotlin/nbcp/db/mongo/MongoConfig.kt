package nbcp.db.mongo

import nbcp.comm.*
import nbcp.db.MyOqlMultipleDataSourceDefine
import nbcp.db.MongoCrudEnum
import nbcp.db.MyOqlBaseActionLogDefine
import org.springframework.beans.factory.InitializingBean

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.*
import org.springframework.stereotype.Component


/**
 * https://docs.spring.io/spring-data/data-mongodb/docs/current/reference/html/index.html
 */
//@Configuration
//@Conditional(ExistsMongoDataSourceConfigCondition::class)
//@AutoConfigureAfter(MongoAutoConfiguration::class)
//class MyOqlMongoDbConfig {
//
//    @Bean
//    fun mongoContext(): MongoMappingContext {
//        var ret = MongoMappingContext()
////        ret.setInitialEntitySet(setOf(Document::class.java))
//        return ret;
//    }
//
//    @Primary
//    @Bean
//    @Throws(Exception::class)
//    fun mongoPrimaryTemplate(dbFactory: MongoDatabaseFactory): MongoTemplate {
//        //remove _class
//        val converter =
//            MappingMongoConverter(DefaultDbRefResolver(dbFactory), SpringUtil.getBean<MongoMappingContext>())
//            converter.setTypeMapper(DefaultMongoTypeMapper(null));
//
//        (converter.conversionService as GenericConversionService).addConverter(Date2LocalDateTimeConverter())
//
//        return MongoTemplate(dbFactory, converter)
//    }
//
//
//    /**
//     * 事务支持。在需要事务的方法上添加注解:@Transactional,使用 mongotemplate 即可。
//     */
//    @Bean
//    fun transactionManager(dbFactory: MongoDatabaseFactory): MongoTransactionManager {
//        return MongoTransactionManager(dbFactory)
//    }
//}

//
//class ExistsMongoDataSourceConfigCondition : Condition {
//    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
//        return (context.environment.getProperty("spring.data.mongodb.uri") != null ||
//            context.environment.getProperty("spring.data.mongodb.database") != null)
//    }
//
//}

/**
 * 定义Mongo不同的数据源
 *
 *app:
 *  mongo:
 *    uris:
 *      yapi: mongodb://dev:123@mongo:27017/yapi
 *    ds:
 *      yapi:
 *          - group
 *          - project
 *          - api
 *          - interface_cat
 *    read:
 *      yapi-read:
 *          - group
 *          - project
 */
@ConfigurationProperties(prefix = "app.mongo.ds")
@Component
class MongoCollectionDataSource : MyOqlMultipleDataSourceDefine() {
}

/**
 * app:
 *   mongo:
 *      log:
 *          query:
 *              -a
 *              -b
 *          delete:
 *              -c
 *              -d
 */
@ConfigurationProperties(prefix = "app.mongo.log")
@Component
class MongoCollectionLogProperties : MyOqlBaseActionLogDefine("app.mongo.log-default") {

}
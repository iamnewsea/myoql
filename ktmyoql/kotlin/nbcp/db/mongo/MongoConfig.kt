package nbcp.db.mongo

import nbcp.comm.HasValue
import nbcp.comm.StringMap
import nbcp.db.AbstractMultipleDataSourceProperties
import nbcp.utils.SpringUtil
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.autoconfigure.AutoConfigureAfter

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.*
import org.springframework.core.convert.support.GenericConversionService
import org.springframework.core.type.AnnotatedTypeMetadata
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.MongoTransactionManager
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.mapping.MongoMappingContext
import org.springframework.stereotype.Component


/**
 * https://docs.spring.io/spring-data/data-mongodb/docs/current/reference/html/index.html
 */
@Configuration
@Conditional(ExistsMongoDataSourceConfigCondition::class)
@AutoConfigureAfter(MongoAutoConfiguration::class)
class MyOqlMongoDbConfig {

    @Bean
    fun mongoContext(): MongoMappingContext {
        var ret = MongoMappingContext()
//        ret.setInitialEntitySet(setOf(Document::class.java))
        return ret;
    }

    @Primary
    @Bean
    @Throws(Exception::class)
    fun mongoPrimaryTemplate(dbFactory: MongoDatabaseFactory): MongoTemplate {
        //remove _class
        val converter =
            MappingMongoConverter(DefaultDbRefResolver(dbFactory), SpringUtil.getBean<MongoMappingContext>())
        converter.setTypeMapper(DefaultMongoTypeMapper(null));

        (converter.conversionService as GenericConversionService).addConverter(Date2LocalDateTimeConverter())

        return MongoTemplate(dbFactory, converter)
    }


    /**
     * 事务支持。在需要事务的方法上添加注解:@Transactional,使用 mongotemplate 即可。
     */
    @Bean
    fun transactionManager(dbFactory: MongoDatabaseFactory): MongoTransactionManager {
        return MongoTransactionManager(dbFactory)
    }
}


class ExistsMongoDataSourceConfigCondition : Condition {
    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        return (context.environment.getProperty("spring.data.mongodb.uri") != null ||
            context.environment.getProperty("spring.data.mongodb.database") != null)
    }

}

/**
 * 定义Mongo不同的数据源
 */
@ConfigurationProperties(prefix = "app.mongo")
@Component
class MongoCollectionDataSource : AbstractMultipleDataSourceProperties() {
}
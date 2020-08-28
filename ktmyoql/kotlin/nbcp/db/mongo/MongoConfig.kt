package nbcp.db.mongo

import nbcp.utils.SpringUtil
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.Primary
import org.springframework.core.convert.support.GenericConversionService
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.mapping.MongoMappingContext

/**
 * https://docs.spring.io/spring-data/data-mongodb/docs/current/reference/html/index.html
 */
@Configuration
@AutoConfigureAfter(MongoDatabaseFactory::class)
@ConditionalOnProperty("spring.data.mongodb.uri")
@DependsOn("springUtil")
class MongoDbConfig {

    @Bean
    fun mongoContext():MongoMappingContext{
        var ret = MongoMappingContext()
//        ret.setInitialEntitySet(setOf(Document::class.java))
        return ret;
    }

    @Primary
    @Bean
    @Throws(Exception::class)
    fun mongoPrimaryTemplate(dbFactory: MongoDatabaseFactory): MongoTemplate {
        //remove _class
        val converter = MappingMongoConverter(DefaultDbRefResolver(dbFactory), SpringUtil.getBean<MongoMappingContext>())
        converter.setTypeMapper(DefaultMongoTypeMapper(null));

        (converter.conversionService as GenericConversionService).addConverter(Date2LocalDateTimeConverter())

        return MongoTemplate(dbFactory, converter)
    }

}
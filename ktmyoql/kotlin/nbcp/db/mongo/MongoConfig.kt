package nbcp.db.mongo

import nbcp.utils.SpringUtil
import org.bson.Document
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.domain.EntityScanner
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.Primary
import org.springframework.core.convert.support.GenericConversionService
import org.springframework.data.mongodb.MongoDbFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.mapping.MongoMappingContext


@Configuration
@AutoConfigureAfter(MongoAutoConfiguration::class)
@ConditionalOnBean(MongoDbFactory::class)
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
    fun mongoTemplate(dbFactory: MongoDbFactory): MongoTemplate {
        //remove _class
        val converter = MappingMongoConverter(DefaultDbRefResolver(dbFactory), SpringUtil.getBean<MongoMappingContext>())
        converter.setTypeMapper(DefaultMongoTypeMapper(null));

        (converter.conversionService as GenericConversionService).addConverter(Date2LocalDateTimeConverter())

        return MongoTemplate(dbFactory, converter)
    }

}
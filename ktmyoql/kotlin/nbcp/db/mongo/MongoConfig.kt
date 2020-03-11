package nbcp.db.mongo

import com.mongodb.MongoClient
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import nbcp.base.extend.AsInt
import nbcp.base.utils.SpringUtil
import nbcp.comm.JsonMap
import nbcp.comm.StringMap
import nbcp.comm.StringTypedMap
import nbcp.db.mongo.Date2LocalDateTimeConverter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.convert.support.GenericConversionService
import org.springframework.data.mongodb.MongoDbFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoDbFactory
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.mapping.MongoMappingContext
import java.util.*


@Configuration
@AutoConfigureAfter(MongoAutoConfiguration::class)
@ConditionalOnBean(MongoDbFactory::class)
class MongoDbConfig {

    @Primary
    @Bean
    @Throws(Exception::class)
    fun mongoTemplate(dbFactory: MongoDbFactory): MongoTemplate {
        //remove _class
        val converter = MappingMongoConverter(DefaultDbRefResolver(dbFactory), MongoMappingContext())
        converter.setTypeMapper(DefaultMongoTypeMapper(null));

        (converter.conversionService as GenericConversionService).addConverter(Date2LocalDateTimeConverter())

        return MongoTemplate(dbFactory, converter)
    }

}
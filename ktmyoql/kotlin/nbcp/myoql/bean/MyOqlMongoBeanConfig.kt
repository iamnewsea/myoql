package nbcp.myoql.bean

import com.mongodb.MongoClientSettings
import nbcp.base.extend.*;
import nbcp.myoql.db.mongo.base.Date2LocalDateTimeConverter
import nbcp.myoql.db.db
import org.bson.UuidRepresentation
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.core.convert.support.GenericConversionService
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.stereotype.Component

@Component
@ConditionalOnClass(MongoTemplate::class)
class MyOqlMongoBeanConfig : BeanPostProcessor {
    companion object {

        private var inited = false;
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        var ret = super.postProcessAfterInitialization(bean, beanName)

        if (bean is MongoTemplate) {

            var converter = bean.converter as MappingMongoConverter;
            converter.typeMapper = DefaultMongoTypeMapper(null)
            (converter.conversionService as GenericConversionService).addConverter(Date2LocalDateTimeConverter())
        } else if (bean is MongoClientSettings) {
        } else if (bean is MongoProperties) {

            /**修改默认连接池参数
             * https://docs.mongodb.com/manual/reference/connection-string/
             *
             * 如果使用 root 用户连接，连接字符串须要额外添加  ?authSource=admin
             * mongodb://root:1234.5678@192.168.5.211:26757/cms?authSource=admin
             */

            if (bean.uri.HasValue) {
                var url = db.mongo.getMongoStandardUri(bean.uri);
                bean.uri = url.toUrl();


                val uuidRepresentation = url.queryJson.get("uuidRepresentation").AsString()
                if (uuidRepresentation.HasValue && !(bean.uuidRepresentation.toString() basicSame uuidRepresentation)) {
                    bean.uuidRepresentation = uuidRepresentation.ToEnum<UuidRepresentation>()!!
                }
            }

        } else if (bean is MongoDatabaseFactory) {
            //系统默认会有一个 MongoTransactionManager。 不必自定义。如果自定义，必须是Primary
            //SpringUtil.registerBeanDefinition("mongoTransactionManager",MongoTransactionManager(bean)){ it.setPrimary(true)}
        }

        return ret;
    }
}
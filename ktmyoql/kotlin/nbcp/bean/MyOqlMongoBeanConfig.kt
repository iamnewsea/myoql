package nbcp.bean

import com.mongodb.MongoClientSettings
import nbcp.comm.HasValue
import nbcp.comm.getStringValue
import nbcp.db.mongo.Date2LocalDateTimeConverter
import nbcp.utils.JsUtil
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
             */

            if (bean.uri.HasValue) {
                var urlJson = JsUtil.parseUrlQueryJson(bean.uri);
                urlJson.queryJson.put("uuidRepresentation", "STANDARD")

                var maxIdleTimeMS = urlJson.queryJson.getStringValue("maxIdleTimeMS", ignoreCase = true)
                if (maxIdleTimeMS.isNullOrEmpty()) {
                    urlJson.queryJson.put("maxIdleTimeMS", "30000")
                }

                var connectTimeoutMS = urlJson.queryJson.getStringValue("connectTimeoutMS", ignoreCase = true)
                if (connectTimeoutMS.isNullOrEmpty()) {
                    urlJson.queryJson.put("connectTimeoutMS", "10000")
                }

                bean.uri = urlJson.toUrl();
            }
            bean.uuidRepresentation = UuidRepresentation.STANDARD
        } else if (bean is MongoDatabaseFactory) {
            //系统默认会有一个 MongoTransactionManager。 不必自定义。如果自定义，必须是Primary
            //SpringUtil.registerBeanDefinition("mongoTransactionManager",MongoTransactionManager(bean)){ it.setPrimary(true)}
        }

        return ret;
    }
}
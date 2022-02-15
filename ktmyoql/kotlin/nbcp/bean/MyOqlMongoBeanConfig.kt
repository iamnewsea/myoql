package nbcp.bean

import com.mongodb.MongoClientSettings
import nbcp.MyOqlInitConfig
import nbcp.comm.HasValue
import nbcp.comm.clazzesIsSimpleDefine
import nbcp.comm.getStringValue
import nbcp.component.BaseJsonMapper
import nbcp.component.DbJsonMapper
import nbcp.db.mongo.*
import nbcp.utils.JsUtil
import nbcp.utils.SpringUtil
import org.bson.Document
import org.bson.UuidRepresentation
import org.bson.types.ObjectId
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
                var urlJson = JsUtil.parseUrlQueryJson(bean.uri);
                urlJson.queryJson.put("uuidRepresentation", "STANDARD")

                if (bean.uri.startsWith("mongodb://") && bean.uri.contains('@')) {
                    var userName = bean.uri.split('@')
                        .first()
                        .split("mongodb://")
                        .last()
                        .split(':')
                        .first()

                    if (userName == "root" && !urlJson.queryJson.containsKey("authSource")) {
                        urlJson.queryJson.put("authSource", "admin")
                    }
                }

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


    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
        if (inited == false) {
            inited = true;

            init_app();
        }

        return super.postProcessBeforeInitialization(bean, beanName)
    }

    private fun init_app() {
        clazzesIsSimpleDefine.add(ObjectId::class.java)

        SpringUtil.context.getBeansOfType(BaseJsonMapper::class.java).values.forEach { mapper ->
            mapper.addTypeModule(ObjectId::class.java, ObjectIdJsonSerializer(), ObjectIdJsonDeserializer())
            if (mapper is DbJsonMapper) {
                mapper.addTypeModule(Document::class.java, DocumentJsonSerializer(), DocumentJsonDeserializer())
            }
        }
    }
}
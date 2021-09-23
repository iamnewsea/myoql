package nbcp.myOqlBeanProcessor

import nbcp.comm.HasValue
import nbcp.comm.getStringValue
import nbcp.db.db
import nbcp.db.mongo.Date2LocalDateTimeConverter
import nbcp.db.mongo.service.UploadFileMongoService
import nbcp.utils.JsUtil
import nbcp.utils.SpringUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Import
import org.springframework.context.event.EventListener
import org.springframework.core.convert.support.GenericConversionService
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.stereotype.Component

@Component
@Import(SpringUtil::class)
@ConditionalOnClass(MongoTemplate::class)
class MyOqlBeanProcessor_Mongo : BeanPostProcessor {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        var ret = super.postProcessAfterInitialization(bean, beanName)

        if (bean is MongoTemplate) {
            var converter = bean.converter as MappingMongoConverter;
            converter.typeMapper = DefaultMongoTypeMapper(null)
            (converter.conversionService as GenericConversionService).addConverter(Date2LocalDateTimeConverter())

            loadMongoDependencyBeans();
        } else if (bean is MongoProperties) {
            //修改默认连接池参数
            if (bean.uri.HasValue) {
                var urlJson = JsUtil.parseUrlQueryJson(bean.uri);
                var maxIdleTimeMS = urlJson.queryJson.getStringValue("maxIdleTimeMS", ignoreCase = true)
                if (maxIdleTimeMS.isNullOrEmpty()) {
                    urlJson.queryJson.put("maxIdleTimeMS", "30000")
                    bean.uri = urlJson.toUrl();
                }
            }
        }

        return ret;
    }

    private fun loadMongoDependencyBeans() {
        SpringUtil.registerBeanDefinition(UploadFileMongoService())
    }


    /**
     * 系统预热之后，最后执行事件。
     */
    @EventListener
    fun onApplicationReady(event: ApplicationReadyEvent) {
        if (SpringUtil.containsBean(MongoTemplate::class.java)) {
            logger.info("mongo groups:" + db.mongo.groups.map { it::class.java.simpleName }
                .joinToString())
            logger.info("sql groups:" + db.sql.groups.map { it::class.java.simpleName }
                .joinToString())
        }
    }
}
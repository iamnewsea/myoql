package nbcp

import nbcp.comm.Important
import nbcp.comm.clazzesIsSimpleDefine
import nbcp.component.BaseJsonMapper
import nbcp.component.DbJsonMapper
import nbcp.db.cache.RedisCacheDbDynamicService
import nbcp.db.db
import nbcp.db.mongo.Date2LocalDateTimeConverter
import nbcp.db.redis.RedisRenewalDynamicService
import nbcp.utils.SpringUtil
import org.bson.Document
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.context.event.ApplicationStartingEvent
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.Import
import org.springframework.context.event.EventListener
import org.springframework.core.convert.support.GenericConversionService
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.stereotype.Component
import java.util.function.Consumer

@Component
@Import(SpringUtil::class)
class MyOqlBeanProcessor : BeanPostProcessor {
    companion object {
        private var inited = false;
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)

    }

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
        if (inited == false) {
            inited = true;

            init_app();
        }

        return super.postProcessBeforeInitialization(bean, beanName)
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        var ret = super.postProcessAfterInitialization(bean, beanName)

        if (bean is StringRedisTemplate) {
            bean.hashValueSerializer = RedisSerializer.json()

            loadRedisDependencyBean()
        } else if (bean is MongoTemplate) {
            var converter = bean.converter as MappingMongoConverter;
            converter.typeMapper = DefaultMongoTypeMapper(null)
            (converter.conversionService as GenericConversionService).addConverter(Date2LocalDateTimeConverter())
        }

        return ret;
    }

    private fun loadRedisDependencyBean() {
        SpringUtil.registerBeanDefinition("redisCacheDbDynamicService", RedisCacheDbDynamicService())
        SpringUtil.registerBeanDefinition("redisRenewalDynamicService", RedisRenewalDynamicService())
    }

    /**
     * 在所有Bean初始化之前执行
     */
    private fun init_app() {
        clazzesIsSimpleDefine.add(ObjectId::class.java)

        BaseJsonMapper.addSerializer(ObjectId::class.java, ObjectIdJsonSerializer(), ObjectIdJsonDeserializer())
        DbJsonMapper.addSerializer(Document::class.java, DocumentJsonSerializer(), DocumentJsonDeserializer())
    }

    /**
     * 系统预热之后，最后执行事件。
     */
    @EventListener
    fun onApplicationReady(event: ApplicationReadyEvent) {
        if (SpringUtil.containsBean(MongoTemplate::class.java)) {
            logger.info("mongo groups:" + db.mongo.groups.map { it::class.java.simpleName }.joinToString())
            logger.info("sql groups:" + db.sql.groups.map { it::class.java.simpleName }.joinToString())
        }
    }

}
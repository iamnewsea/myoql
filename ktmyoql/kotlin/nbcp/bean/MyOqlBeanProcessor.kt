package nbcp.bean


import nbcp.comm.Important
import nbcp.comm.clazzesIsSimpleDefine
import nbcp.component.BaseJsonMapper
import nbcp.component.DbJsonMapper
import nbcp.db.db
import nbcp.db.mongo.*
import nbcp.utils.SpringUtil
import org.bson.Document
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Import
import org.springframework.context.event.EventListener
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

@Component
@ConditionalOnClass(value = [ObjectId::class, Document::class])
class MyOqlMongoJsonSerializerConfig : BeanPostProcessor {
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

        return ret;
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
            logger.Important("mongo groups:" + db.mongo.groups.map { it::class.java.simpleName }
                .joinToString())
        }

        if (SpringUtil.containsBean(JdbcTemplate::class.java)) {
            logger.Important("sql groups:" + db.sql.groups.map { it::class.java.simpleName }
                .joinToString())
        }
    }
}
package nbcp.myoql.bean

import com.fasterxml.jackson.databind.ObjectMapper
import nbcp.base.component.DbJsonMapper
import nbcp.base.extend.*
import nbcp.base.utils.SpringUtil
import nbcp.myoql.db.mongo.DocumentJsonDeserializer
import nbcp.myoql.db.mongo.DocumentJsonSerializer
import nbcp.myoql.db.mongo.ObjectIdJsonDeserializer
import nbcp.myoql.db.mongo.ObjectIdJsonSerializer
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
@ConditionalOnClass(ObjectId::class)
class MyOqlBsonBeanConfig : BeanPostProcessor {
    companion object {
        private var inited = false;
        private var objectMapperProced = false;
    }

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
        if (inited == false) {
            inited = true;

            init_app();
        }

        if (bean is ObjectMapper) {
            bean.addObjectMapperTypeModule(
                ObjectId::class.java,
                ObjectIdJsonSerializer(),
                ObjectIdJsonDeserializer()
            )

            bindExtendObjectMappers();
        }

        return super.postProcessBeforeInitialization(bean, beanName)
    }

    @EventListener
    fun bsonBeanInitOnApplicationStarted(ev: ApplicationStartedEvent) {
        bindExtendObjectMappers();
    }

    private fun bindExtendObjectMappers() {
        if (objectMapperProced) {
            objectMapperProced = true;

            SpringUtil.context.getBeanNamesForType(ObjectMapper::class.java).forEach { name ->
                var mapper = SpringUtil.context.getBean(name) as ObjectMapper;
                mapper.addObjectMapperTypeModule(
                    ObjectId::class.java,
                    ObjectIdJsonSerializer(),
                    ObjectIdJsonDeserializer()
                )
            }

            DbJsonMapper.INSTANCE.addObjectMapperTypeModule(
                Document::class.java,
                DocumentJsonSerializer(),
                DocumentJsonDeserializer()
            )
        }

    }

    private fun init_app() {
        clazzesIsSimpleDefine.add(ObjectId::class.java)
    }
}
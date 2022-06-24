package nbcp.bean

import com.fasterxml.jackson.databind.ObjectMapper
import nbcp.comm.clazzesIsSimpleDefine
import nbcp.component.AppJsonMapper
import nbcp.component.DbJsonMapper
import nbcp.component.WebJsonMapper
import nbcp.db.mongo.*
import nbcp.extend.addObjectMapperTypeModule
import nbcp.utils.SpringUtil
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
    fun app_started(ev: ApplicationStartedEvent) {
        bindExtendObjectMappers();
    }

    private fun bindExtendObjectMappers() {
        if (objectMapperProced) {
            objectMapperProced = true;

            AppJsonMapper.extendObjectMappers.forEach { mapper ->
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
package nbcp.bean

import com.fasterxml.jackson.databind.ObjectMapper
import nbcp.comm.clazzesIsSimpleDefine
import nbcp.component.DbJsonMapper
import nbcp.db.mongo.*
import nbcp.extend.addObjectMapperTypeModule
import nbcp.utils.SpringUtil
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.stereotype.Component

@Component
@ConditionalOnClass(ObjectId::class)
class MyOqlBsonBeanConfig : BeanPostProcessor {
    companion object {
        private var inited = false;
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

        SpringUtil.context.getBeansOfType(ObjectMapper::class.java).values
            .forEach { mapper ->
                mapper.addObjectMapperTypeModule(ObjectId::class.java, ObjectIdJsonSerializer(), ObjectIdJsonDeserializer())
                if (mapper is DbJsonMapper) {
                    mapper.addObjectMapperTypeModule(Document::class.java, DocumentJsonSerializer(), DocumentJsonDeserializer())
                }
            }
    }
}
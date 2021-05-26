package nbcp

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import nbcp.comm.*
import nbcp.component.BaseJsonMapper
import nbcp.component.DbJsonMapper
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component


@Component
class ObjectIdJsonMapperBeforeInitBean : BeanPostProcessor {
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

    /**
     * 在所有Bean初始化之前执行
     */
    private fun init_app() {
        clazzesIsSimpleDefine.add(ObjectId::class.java)

        BaseJsonMapper.addSerializer(ObjectId::class.java, ObjectIdJsonSerializer(), ObjectIdJsonDeserializer())
        DbJsonMapper.addSerializer(Document::class.java, DocumentJsonSerializer(), DocumentJsonDeserializer())
    }
}
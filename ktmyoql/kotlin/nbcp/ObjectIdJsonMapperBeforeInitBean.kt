package nbcp

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import nbcp.comm.DefaultMyJsonMapper
import nbcp.comm.HasValue
import nbcp.comm.clazzesIsSimpleDefine
import org.bson.types.ObjectId
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component


class ObjectIdJsonSerializer : JsonSerializer<ObjectId>() {
    override fun serialize(o: ObjectId?, j: JsonGenerator, s: SerializerProvider) {
        if (o == null) {
            j.writeNull()
        } else {
            j.writeString(o.toString())
        }
    }
}

class ObjectIdJsonDeserializer : JsonDeserializer<ObjectId>() {
    override fun deserialize(json: JsonParser?, ctxt: DeserializationContext?): ObjectId? {
        if (json == null) {
            return null;
        }

        if (json.valueAsString.HasValue == false) {
            return null;
        }

        return ObjectId(json.valueAsString)
    }
}

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

        DefaultMyJsonMapper.addSerializer(ObjectId::class.java, ObjectIdJsonSerializer(), ObjectIdJsonDeserializer())
    }
}
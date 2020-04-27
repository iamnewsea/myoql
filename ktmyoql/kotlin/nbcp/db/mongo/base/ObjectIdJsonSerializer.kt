package nbcp.db.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import nbcp.comm.DefaultMyJsonMapper
import nbcp.utils.*
import org.bson.types.ObjectId
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Component

@Component
@DependsOn("defaultMyJsonMapper")
class ObjectIdJsonSerializer : JsonSerializer<ObjectId>(),InitializingBean {
    override fun serialize(o: ObjectId?, j: JsonGenerator, s: SerializerProvider) {
        if (o == null) {
            j.writeNull()
        } else {
            j.writeString(o.toString())
        }
    }

    override fun afterPropertiesSet() {
        DefaultMyJsonMapper.addSerializer(ObjectId::class.java, this)
    }
}
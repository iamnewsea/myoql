package nbcp.db.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import nbcp.utils.*
import nbcp.comm.MyJsonModule
import org.bson.types.ObjectId
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Component

@Component
@DependsOn("myJsonModule")
class ObjectIdJsonSerializer : JsonSerializer<ObjectId>(),InitializingBean {
    override fun serialize(o: ObjectId?, j: JsonGenerator, s: SerializerProvider) {
        if (o == null) {
            j.writeNull()
        } else {
            j.writeString(o.toString())
        }
    }

    override fun afterPropertiesSet() {
        SpringUtil.getBean<MyJsonModule>().addSerializer(ObjectId::class.java, this)
    }
}
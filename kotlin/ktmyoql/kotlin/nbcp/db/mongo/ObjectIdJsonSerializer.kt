package nbcp.db.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import org.bson.types.ObjectId
import org.springframework.stereotype.Component

@Component
class ObjectIdJsonSerializer : JsonSerializer<ObjectId>() {
    override fun serialize(o: ObjectId?, j: JsonGenerator, s: SerializerProvider) {
        if (o == null) {
            j.writeNull()
        } else {
            j.writeString(o.toString())
        }
    }

    override fun handledType(): Class<ObjectId> {
        return ObjectId::class.java
    }
}
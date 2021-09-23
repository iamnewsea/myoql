package nbcp.db.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import nbcp.comm.HasValue
import org.bson.Document
import org.bson.types.ObjectId


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


class DocumentJsonSerializer : JsonSerializer<Document>() {
    override fun serialize(o: Document?, j: JsonGenerator, s: SerializerProvider) {
        if (o == null) {
            j.writeNull()
        } else {
            j.writeString(o.toJson())
        }
    }
}

class DocumentJsonDeserializer : JsonDeserializer<Document>() {
    override fun deserialize(json: JsonParser?, ctxt: DeserializationContext?): Document? {
        if (json == null) {
            return null;
        }

        return Document.parse(json.valueAsString);
    }
}
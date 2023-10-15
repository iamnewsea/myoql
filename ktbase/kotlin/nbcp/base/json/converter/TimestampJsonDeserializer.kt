package nbcp.base.json.converter

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import nbcp.base.extend.AsLocalDateTime
import java.sql.Timestamp
import java.util.*

class TimestampJsonDeserializer : JsonDeserializer<Timestamp>() {
    override fun deserialize(json: JsonParser?, ctxt: DeserializationContext?): Timestamp? {
        if (json == null) {
            return null;
        }

        if (json.currentToken != JsonToken.VALUE_STRING) {
            return null;
        }

        if (json.valueAsString == null) {
            return null;
        }

        if (json.valueAsString.contains("-") || json.valueAsString.contains("/")) {
            return Timestamp.valueOf(json.valueAsString.AsLocalDateTime());
        }

        return Timestamp.valueOf(Date(json.longValue).AsLocalDateTime());
    }
}
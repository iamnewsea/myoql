package nbcp.base.comm.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import nbcp.base.extend.AsLocalDateTime
import java.time.LocalDateTime
import java.util.*

class LocalDateTimeJsonDeserializer : JsonDeserializer<LocalDateTime>() {
    override fun deserialize(json: JsonParser?, ctxt: DeserializationContext?): LocalDateTime? {
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
            return json.valueAsString.AsLocalDateTime();
        }


        return Date(json.longValue).AsLocalDateTime();
    }
}
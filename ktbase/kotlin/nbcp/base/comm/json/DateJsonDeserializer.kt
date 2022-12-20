package nbcp.base.comm.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import nbcp.base.extend.AsDate
import java.util.*

class DateJsonDeserializer : JsonDeserializer<Date>() {
    override fun deserialize(json: JsonParser?, ctxt: DeserializationContext?): Date? {
        if (json == null) {
            return null;
        }

        if (json.currentToken != JsonToken.VALUE_STRING) {
            return null;
        }

        if (json.valueAsString == null) {
            return null;
        }

        var stringValue = json.valueAsString
        if (stringValue.contains("-") || stringValue.contains("/")) {
            return stringValue.AsDate();
        }

        return Date(json.longValue);
    }
}
package nbcp.base.json.converter

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import nbcp.base.comm.MyString

class MyStringDeserializer : JsonDeserializer<MyString>() {
    override fun deserialize(json: JsonParser?, p1: DeserializationContext?): MyString? {
        if (json == null) {
            return null;
        }

        if (json.currentToken != JsonToken.VALUE_STRING) {
            return null;
        }

        if (json.valueAsString == null) {
            return MyString();
        }
        return MyString(json.valueAsString)
    }
}
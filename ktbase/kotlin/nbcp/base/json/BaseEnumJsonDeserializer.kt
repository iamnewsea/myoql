package nbcp.base.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import nbcp.base.extend.ToEnum

class BaseEnumJsonDeserializer<T>(private val clz: Class<T>) : JsonDeserializer<T>() {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): T? {
        val value = p.valueAsString ?: return null
        return value.ToEnum(clz)
    }
}
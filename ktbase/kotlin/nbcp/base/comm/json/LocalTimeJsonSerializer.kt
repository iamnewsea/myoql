package nbcp.base.comm.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import nbcp.base.extend.Format
import java.time.LocalTime

class LocalTimeJsonSerializer : JsonSerializer<LocalTime>() {
    override fun serialize(value: LocalTime?, generator: JsonGenerator, serializers: SerializerProvider) {
        if (value == null) {
            generator.writeNull()
        } else {
            generator.writeString(value.Format())
        }
    }
}
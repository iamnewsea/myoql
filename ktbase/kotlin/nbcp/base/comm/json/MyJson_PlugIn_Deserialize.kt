package nbcp.base.comm.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import nbcp.base.comm.MyRawString


class MyRawStringSerializer : JsonSerializer<MyRawString>() {
    override fun serialize(value: MyRawString?, generator: JsonGenerator, p2: SerializerProvider?) {
        if (value == null) {
            generator.writeNull()
        } else {
            generator.writeRawValue(value.toString())
        }
    }
}
package nbcp.base.comm.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import nbcp.base.comm.MyString

class MyStringSerializer : JsonSerializer<MyString>() {
    override fun serialize(value: MyString?, generator: JsonGenerator, p2: SerializerProvider?) {
        if (value == null) {
            generator.writeNull()
        } else {
            generator.writeString(value.toString())
        }
    }
}
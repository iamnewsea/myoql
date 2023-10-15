package nbcp.base.json.converter

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import nbcp.base.enums.JsonStyleScopeEnum
import nbcp.base.extend.*;
import nbcp.base.extend.scopes
import java.sql.Timestamp

class TimestampJsonSerializer : JsonSerializer<Timestamp>() {
    override fun serialize(value: Timestamp?, generator: JsonGenerator, serializers: SerializerProvider) {
        if (value == null) {
            generator.writeNull()
        } else {
            //使用上下文格式转换。 , 不使用传过来的  (serializers.config.dateFormat as SimpleDateFormat).toPattern()

            var style = scopes.getLatest(
                JsonStyleScopeEnum.DATE_LOCAL_STYLE,
                JsonStyleScopeEnum.DATE_UTC_STYLE,
                JsonStyleScopeEnum.DATE_STANDARD_STYLE
            )
                ?: JsonStyleScopeEnum.DATE_STANDARD_STYLE
            var format = listOf(style).getDateFormat()
            generator.writeString(value.toLocalDateTime().Format(format))
        }
    }
}
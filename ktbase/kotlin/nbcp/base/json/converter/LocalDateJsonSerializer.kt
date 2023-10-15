package nbcp.base.json.converter

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import nbcp.base.enums.JsonStyleScopeEnum
import nbcp.base.extend.Format
import nbcp.base.extend.scopes
import java.time.LocalDate

class LocalDateJsonSerializer : JsonSerializer<LocalDate>() {
    override fun serialize(value: LocalDate?, generator: JsonGenerator, serializers: SerializerProvider) {
        if (value == null) {
            generator.writeNull()
        } else {
            //使用上下文格式转换。 , 不使用传过来的  (serializers.config.dateFormat as SimpleDateFormat).toPattern()
            var format = "yyyy-MM-dd";
            var style = scopes.getLatest(
                JsonStyleScopeEnum.DATE_LOCAL_STYLE,
                JsonStyleScopeEnum.DATE_UTC_STYLE,
                JsonStyleScopeEnum.DATE_STANDARD_STYLE
            )
                ?: JsonStyleScopeEnum.DATE_STANDARD_STYLE
            if (style == JsonStyleScopeEnum.DATE_LOCAL_STYLE ||
                style == JsonStyleScopeEnum.DATE_UTC_STYLE
            ) {
                format = "yyyy/MM/dd"
            }
            generator.writeString(value.Format(format))
        }
    }
}
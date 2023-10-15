package nbcp.base.json.converter

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import nbcp.base.enums.JsonStyleScopeEnum
import nbcp.base.extend.*;
import nbcp.base.extend.scopes
import java.time.LocalDateTime

class LocalDateTimeJsonSerializer : JsonSerializer<LocalDateTime>() {
    override fun serialize(value: LocalDateTime?, generator: JsonGenerator, serializers: SerializerProvider) {
        if (value == null) {
            generator.writeNull()
        } else {
            //想办法在输出的时候，表示该字段是一个时间类型。 客户端收到后，统一转换。添加 _res
            //使用上下文格式转换。 , 不使用传过来的  (serializers.config.dateFormat as SimpleDateFormat).toPattern()

            var style = scopes.getLatest(
                JsonStyleScopeEnum.DATE_LOCAL_STYLE,
                JsonStyleScopeEnum.DATE_UTC_STYLE,
                JsonStyleScopeEnum.DATE_STANDARD_STYLE
            )
                ?: JsonStyleScopeEnum.DATE_STANDARD_STYLE
            var format = listOf(style).getDateFormat()

            generator.writeString(value.Format(format))
        }
    }
}
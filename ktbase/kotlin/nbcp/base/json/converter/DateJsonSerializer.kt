package nbcp.base.json.converter

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import nbcp.base.enums.JsonStyleScopeEnum
import nbcp.base.extend.*;
import nbcp.base.extend.scopes
import java.util.*

/**
 * Created by yuxh on 2018/9/18
 * Javascript 中时间格式 斜线表示本地时间 。 减号表示 UTC。
 * 如：new Date("2020/02/17") 是当地时间
 * 本插件使用减号，需要客户端在处理时间的时候，使用 斜线 替换 减号。
 */
class DateJsonSerializer : JsonSerializer<Date>() {
    override fun serialize(value: Date?, generator: JsonGenerator, serializers: SerializerProvider) {
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

            generator.writeString(value.Format(format))
        }
    }
}
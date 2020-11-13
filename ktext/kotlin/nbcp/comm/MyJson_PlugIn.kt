package nbcp.comm

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
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
            var format = "yyyy-MM-dd HH:mm:ss";
            var style = scopes.getLatestScope(JsonStyleEnumScope.DateLocalStyle, JsonStyleEnumScope.DateUtcStyle, JsonStyleEnumScope.DateStandardStyle)
                    ?: JsonStyleEnumScope.DateStandardStyle
            if (style == JsonStyleEnumScope.DateLocalStyle) {
                format = "yyyy/MM/dd HH:mm:ss"
            } else if (style == JsonStyleEnumScope.DateUtcStyle) {
                format = "yyyy/MM/dd HH:mm:ss"
            }

            generator.writeString(value.Format(format))
        }
    }
}

class LocalDateJsonSerializer : JsonSerializer<LocalDate>() {
    override fun serialize(value: LocalDate?, generator: JsonGenerator, serializers: SerializerProvider) {
        if (value == null) {
            generator.writeNull()
        } else {
            //使用上下文格式转换。 , 不使用传过来的  (serializers.config.dateFormat as SimpleDateFormat).toPattern()
            var format = "yyyy-MM-dd";
            var style = scopes.getLatestScope(JsonStyleEnumScope.DateLocalStyle, JsonStyleEnumScope.DateUtcStyle, JsonStyleEnumScope.DateStandardStyle)
                    ?: JsonStyleEnumScope.DateStandardStyle
            if (style == JsonStyleEnumScope.DateLocalStyle) {
                format = "yyyy/MM/dd"
            }
            generator.writeString(value.Format(format))
        }
    }
}

class LocalTimeJsonSerializer : JsonSerializer<LocalTime>() {
    override fun serialize(value: LocalTime?, generator: JsonGenerator, serializers: SerializerProvider) {
        if (value == null) {
            generator.writeNull()
        }  else {
            generator.writeString(value.Format())
        }
    }
}


class LocalDateTimeJsonSerializer : JsonSerializer<LocalDateTime>() {
    override fun serialize(value: LocalDateTime?, generator: JsonGenerator, serializers: SerializerProvider) {
        if (value == null) {
            generator.writeNull()
        } else {
            //想办法在输出的时候，表示该字段是一个时间类型。 客户端收到后，统一转换。添加 _res
            //使用上下文格式转换。 , 不使用传过来的  (serializers.config.dateFormat as SimpleDateFormat).toPattern()
            var format = "yyyy-MM-dd HH:mm:ss";
            var style = scopes.getLatestScope(JsonStyleEnumScope.DateLocalStyle, JsonStyleEnumScope.DateUtcStyle, JsonStyleEnumScope.DateStandardStyle)
                    ?: JsonStyleEnumScope.DateStandardStyle
            if (style == JsonStyleEnumScope.DateLocalStyle) {
                format = "yyyy/MM/dd HH:mm:ss"
            } else if (style == JsonStyleEnumScope.DateUtcStyle) {
                format = "yyyy/MM/dd HH:mm:ss"
            }

            generator.writeString(value.Format(format))
        }
    }
}

class TimestampJsonSerializer : JsonSerializer<Timestamp>() {
    override fun serialize(value: Timestamp?, generator: JsonGenerator, serializers: SerializerProvider) {
        if (value == null) {
            generator.writeNull()
        } else {
            //使用上下文格式转换。 , 不使用传过来的  (serializers.config.dateFormat as SimpleDateFormat).toPattern()
            var format = "yyyy-MM-dd HH:mm:ss";
            var style = scopes.getLatestScope(JsonStyleEnumScope.DateLocalStyle, JsonStyleEnumScope.DateUtcStyle, JsonStyleEnumScope.DateStandardStyle)
                    ?: JsonStyleEnumScope.DateStandardStyle
            if (style == JsonStyleEnumScope.DateLocalStyle) {
                format = "yyyy/MM/dd HH:mm:ss"
            } else if (style == JsonStyleEnumScope.DateUtcStyle) {
                format = "yyyy/MM/dd HH:mm:ss"
            }
            generator.writeString(value.toLocalDateTime().Format(format))
        }
    }
}

//---------------------------------------------------------

class DateJsonDeserializer : JsonDeserializer<Date>() {
    override fun deserialize(json: JsonParser?, ctxt: DeserializationContext?): Date? {
        if (json == null) {
            return null;
        }
        var stringValue = json.valueAsString
        if (stringValue.contains("-") || stringValue.contains("/")) {
            return stringValue.AsDate();
        }

        return Date(json.longValue);
    }
}


class LocalDateJsonDeserializer : JsonDeserializer<LocalDate>() {
    override fun deserialize(json: JsonParser?, ctxt: DeserializationContext?): LocalDate? {
        if (json == null) {
            return null;
        }

        if (json.valueAsString.contains("-")) {
            return json.valueAsString.AsLocalDate();
        }

        return Date(json.longValue).AsLocalDate();
    }
}


class LocalTimeJsonDeserializer : JsonDeserializer<LocalTime>() {
    override fun deserialize(json: JsonParser?, ctxt: DeserializationContext?): LocalTime? {
        if (json == null) {
            return null;
        }

        return json.valueAsString.AsLocalTime();
    }
}


class LocalDateTimeJsonDeserializer : JsonDeserializer<LocalDateTime>() {
    override fun deserialize(json: JsonParser?, ctxt: DeserializationContext?): LocalDateTime? {
        if (json == null) {
            return null;
        }

        if (json.valueAsString.contains("-") || json.valueAsString.contains("/")) {
            return json.valueAsString.AsLocalDateTime();
        }


        return Date(json.longValue).AsLocalDateTime();
    }
}


class TimestampJsonDeserializer : JsonDeserializer<Timestamp>() {
    override fun deserialize(json: JsonParser?, ctxt: DeserializationContext?): Timestamp? {
        if (json == null) {
            return null;
        }

        if (json.valueAsString.contains("-") || json.valueAsString.contains("/")) {
            return Timestamp.valueOf(json.valueAsString.AsLocalDateTime());
        }

        return Timestamp.valueOf(Date(json.longValue).AsLocalDateTime());
    }
}

package nbcp.comm

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.json.PackageVersion
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import nbcp.comm.AsDate
import nbcp.comm.AsLocalDate
import nbcp.comm.AsLocalDateTime
import nbcp.comm.AsString
import nbcp.utils.*
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Component
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
@Component
@DependsOn("myJsonModule")
class DateJsonSerializer : JsonSerializer<Date>(), InitializingBean {
    override fun serialize(value: Date?, generator: JsonGenerator, serializers: SerializerProvider) {
        if (value == null) {
            generator.writeNull()
        } else {
            generator.writeString(SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(value))
        }
    }

    override fun afterPropertiesSet() {
        SpringUtil.getBean<MyJsonModule>().addSerializer(Date::class.java, this)
    }
}


@Component
@DependsOn("myJsonModule")
class LocalDateJsonSerializer : JsonSerializer<LocalDate>(), InitializingBean {
    override fun serialize(value: LocalDate?, generator: JsonGenerator, serializers: SerializerProvider) {
        if (value == null) {
            generator.writeNull()
        } else {
            generator.writeString(value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
        }
    }

    override fun afterPropertiesSet() {
        SpringUtil.getBean<MyJsonModule>().addSerializer(LocalDate::class.java, this)
    }
}

@Component
@DependsOn("myJsonModule")
class LocalTimeJsonSerializer : JsonSerializer<LocalTime>(), InitializingBean {
    override fun serialize(value: LocalTime?, generator: JsonGenerator, serializers: SerializerProvider) {
        if (value == null) {
            generator.writeNull()
        } else {
            generator.writeString(value.format(DateTimeFormatter.ofPattern("HH:mm:ss")))
        }
    }

    override fun afterPropertiesSet() {
        SpringUtil.getBean<MyJsonModule>().addSerializer(LocalTime::class.java, this)
    }
}

@Component
@DependsOn("myJsonModule")
class LocalDateTimeJsonSerializer : JsonSerializer<LocalDateTime>(), InitializingBean {
    override fun serialize(value: LocalDateTime?, generator: JsonGenerator, serializers: SerializerProvider) {
        if (value == null) {
            generator.writeNull()
        } else {
            //想办法在输出的时候，表示该字段是一个时间类型。 客户端收到后，统一转换。添加 _res
            generator.writeString(value.AsString())
        }
    }

    override fun afterPropertiesSet() {
        SpringUtil.getBean<MyJsonModule>().addSerializer(LocalDateTime::class.java, this)
    }
}

@Component
@DependsOn("myJsonModule")
class TimestampJsonSerializer : JsonSerializer<Timestamp>(), InitializingBean {
    override fun serialize(value: Timestamp?, generator: JsonGenerator, serializers: SerializerProvider) {
        if (value == null) {
            generator.writeNull()
        } else {
            generator.writeString(value.toLocalDateTime().AsString())
        }
    }

    override fun afterPropertiesSet() {
        SpringUtil.getBean<MyJsonModule>().addSerializer(Timestamp::class.java, this)
    }
}

//---------------------------------------------------------

@Component
@DependsOn("myJsonModule")
class DateJsonDeserializer : JsonDeserializer<Date>(), InitializingBean {
    override fun deserialize(json: JsonParser?, ctxt: DeserializationContext?): Date? {
        if (json == null) {
            return null;
        }

        if (json.valueAsString.contains("-")) {
            return json.valueAsString.AsDate();
        }

        return Date(json.longValue);
    }

    override fun afterPropertiesSet() {
        SpringUtil.getBean<MyJsonModule>().addDeserializer(Date::class.java, this)
    }
}

@Component
@DependsOn("myJsonModule")
class LocalDateJsonDeserializer : JsonDeserializer<LocalDate>(), InitializingBean {
    override fun deserialize(json: JsonParser?, ctxt: DeserializationContext?): LocalDate? {
        if (json == null) {
            return null;
        }

        if (json.valueAsString.contains("-")) {
            return json.valueAsString.AsLocalDate();
        }

        return Date(json.longValue).AsLocalDate();
    }

    override fun afterPropertiesSet() {
        SpringUtil.getBean<MyJsonModule>().addDeserializer(LocalDate::class.java, this)
    }
}

@Component
@DependsOn("myJsonModule")
class LocalTimeJsonDeserializer : JsonDeserializer<LocalTime>(), InitializingBean {
    override fun deserialize(json: JsonParser?, ctxt: DeserializationContext?): LocalTime? {
        if (json == null) {
            return null;
        }

        if (json.valueAsString.contains(".")) {
            return LocalTime.parse(json.valueAsString, DateTimeFormatter.ofPattern("HH:mm:ss.SSS"))
        }


        return LocalTime.parse(json.valueAsString, DateTimeFormatter.ofPattern("HH:mm:ss"))
    }

    override fun afterPropertiesSet() {
        SpringUtil.getBean<MyJsonModule>().addDeserializer(LocalTime::class.java, this)
    }
}

@Component
@DependsOn("myJsonModule")
class LocalDateTimeJsonDeserializer : JsonDeserializer<LocalDateTime>(), InitializingBean {
    override fun deserialize(json: JsonParser?, ctxt: DeserializationContext?): LocalDateTime? {
        if (json == null) {
            return null;
        }

        if (json.valueAsString.contains("-") || json.valueAsString.contains("/")) {
            return json.valueAsString.AsLocalDateTime();
        }


        return Date(json.longValue).AsLocalDateTime();
    }

    override fun afterPropertiesSet() {
        SpringUtil.getBean<MyJsonModule>().addDeserializer(LocalDateTime::class.java, this)
    }
}

@Component
@DependsOn("myJsonModule")
class TimestampJsonDeserializer : JsonDeserializer<Timestamp>(), InitializingBean {
    override fun deserialize(json: JsonParser?, ctxt: DeserializationContext?): Timestamp? {
        if (json == null) {
            return null;
        }

        if (json.valueAsString.contains("-") || json.valueAsString.contains("/")) {
            return Timestamp.valueOf(json.valueAsString.AsLocalDateTime());
        }

        return Timestamp.valueOf(Date(json.longValue).AsLocalDateTime());
    }

    override fun afterPropertiesSet() {
        SpringUtil.getBean<MyJsonModule>().addDeserializer(Timestamp::class.java, this)
    }
}


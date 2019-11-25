package nbcp.base.extend

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.json.PackageVersion
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component
import java.lang.reflect.Type
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Created by yuxh on 2018/9/18
 */


class LocalDateJsonSerializer : JsonSerializer<LocalDate>() {
    override fun serialize(value: LocalDate?, generator: JsonGenerator, serializers: SerializerProvider) {
        if (value == null) {
            generator.writeNull()
        } else {
            generator.writeString(value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
        }
    }
}

class LocalTimeJsonSerializer : JsonSerializer<LocalTime>() {
    override fun serialize(value: LocalTime?, generator: JsonGenerator, serializers: SerializerProvider) {
        if (value == null) {
            generator.writeNull()
        } else {
            generator.writeString(value.format(DateTimeFormatter.ofPattern("HH:mm:ss")))
        }
    }
}

class LocalDateTimeJsonSerializer : JsonSerializer<LocalDateTime>() {
    override fun serialize(value: LocalDateTime?, generator: JsonGenerator, serializers: SerializerProvider) {
        if (value == null) {
            generator.writeNull()
        } else {
            //想办法在输出的时候，表示该字段是一个时间类型。 客户端收到后，统一转换。添加 _res
            generator.writeString(value.AsString())
        }
    }
}

class TimestampJsonSerializer : JsonSerializer<Timestamp>() {
    override fun serialize(value: Timestamp?, generator: JsonGenerator, serializers: SerializerProvider) {
        if (value == null) {
            generator.writeNull()
        } else {
            generator.writeString(value.toLocalDateTime().AsString())
        }
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

        if (json.valueAsString.contains(".")) {
            return LocalTime.parse(json.valueAsString, DateTimeFormatter.ofPattern("HH:mm:ss.SSS"))
        }


        return LocalTime.parse(json.valueAsString, DateTimeFormatter.ofPattern("HH:mm:ss"))
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

//Jackson 输出的时候，进行自定义序列化格式。
// http://www.jianshu.com/p/a0fb6559f56d
@Component
class JavascriptDateModule() : SimpleModule(PackageVersion.VERSION), BeanPostProcessor {
    override fun postProcessAfterInitialization(bean: Any , beanName: String ): Any {
        return bean;
    }


    init {
        addSerializer(LocalDate::class.java, LocalDateJsonSerializer());
        addSerializer(LocalTime::class.java, LocalTimeJsonSerializer());
        addSerializer(LocalDateTime::class.java, LocalDateTimeJsonSerializer());
        addSerializer(java.sql.Timestamp::class.java, TimestampJsonSerializer())

        addDeserializer(LocalDate::class.java, LocalDateJsonDeserializer())
        addDeserializer(LocalTime::class.java, LocalTimeJsonDeserializer())
        addDeserializer(LocalDateTime::class.java, LocalDateTimeJsonDeserializer())
        addDeserializer(java.sql.Timestamp::class.java, TimestampJsonDeserializer())
    }

    //先执行收集插件,再执行 afterPropertiesSet
    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
        if (bean is JsonSerializer<*>) {
            addSerializer(bean)
        } else if (bean is JsonDeserializer<*>) {
//            var gt = (bean::class.java.genericSuperclass as ParameterizedTypeImpl).actualTypeArguments[0] as Class<*>
//
//            addDeserializer(bean.handledType(),bean)
        }
        return bean;
    }
}

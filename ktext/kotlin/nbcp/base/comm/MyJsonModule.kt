package nbcp.comm

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.json.PackageVersion
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import nbcp.base.extend.GetActualClass
import nbcp.base.utils.SpringUtil
import nbcp.comm.*
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Component
import java.lang.reflect.ParameterizedType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

//Jackson 输出的时候，进行自定义序列化格式。
// http://www.jianshu.com/p/a0fb6559f56d
@Component
@DependsOn("springUtil")
class MyJsonModule() : SimpleModule(PackageVersion.VERSION) {
//    override fun afterPropertiesSet() {
//        addSerializer(Date::class.java, DateJsonSerializer());
//        addSerializer(LocalDate::class.java, LocalDateJsonSerializer());
//        addSerializer(LocalTime::class.java, LocalTimeJsonSerializer());
//        addSerializer(LocalDateTime::class.java, LocalDateTimeJsonSerializer())
//        addSerializer(java.sql.Timestamp::class.java, TimestampJsonSerializer())
//
//        addDeserializer(Date::class.java, DateJsonDeserializer())
//        addDeserializer(LocalDate::class.java, LocalDateJsonDeserializer())
//        addDeserializer(LocalTime::class.java, LocalTimeJsonDeserializer())
//        addDeserializer(LocalDateTime::class.java, LocalDateTimeJsonDeserializer())
//        addDeserializer(java.sql.Timestamp::class.java, TimestampJsonDeserializer())
//    }
}
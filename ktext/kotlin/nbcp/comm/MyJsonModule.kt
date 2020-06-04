//package nbcp.comm
//
//import com.fasterxml.jackson.core.JsonGenerator
//import com.fasterxml.jackson.core.JsonParser
//import com.fasterxml.jackson.core.json.PackageVersion
//import com.fasterxml.jackson.databind.DeserializationContext
//import com.fasterxml.jackson.databind.JsonDeserializer
//import com.fasterxml.jackson.databind.JsonSerializer
//import com.fasterxml.jackson.databind.SerializerProvider
//import com.fasterxml.jackson.databind.module.SimpleModule
//import nbcp.comm.GetActualClass
//import nbcp.utils.*
//import nbcp.comm.*
//import org.springframework.beans.factory.InitializingBean
//import org.springframework.beans.factory.config.BeanPostProcessor
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.DependsOn
//import org.springframework.stereotype.Component
//import java.lang.reflect.ParameterizedType
//import java.time.LocalDate
//import java.time.LocalDateTime
//import java.time.LocalTime
//import java.util.*
//
////Jackson 输出的时候，进行自定义序列化格式。
//// http://www.jianshu.com/p/a0fb6559f56d
//@Component
//@DependsOn("springUtil")
//object MyJsonModule : SimpleModule(PackageVersion.VERSION) {
//    var changed: Boolean = false
//        private set;
//
//
//    override fun <T : Any?> addSerializer(type: Class<out T>?, ser: JsonSerializer<T>?): SimpleModule {
//        changed = true;
//        super.addSerializer(type, ser)
//
//        reset();
//        return this;
//    }
//
//    override fun addSerializer(ser: JsonSerializer<*>?): SimpleModule {
//        changed = true;
//        super.addSerializer(ser)
//        reset();
//        return this;
//    }
//
//    override fun <T : Any?> addDeserializer(type: Class<T>?, deser: JsonDeserializer<out T>?): SimpleModule {
//        changed = true;
//        super.addDeserializer(type, deser)
//        reset();
//        return this;
//    }
//
//    fun reset() {
//        if (SpringUtil.isInited == false) return;
//
//        SpringUtil.getBean<DefaultMyJsonMapper>().registerModule(this);
//    }
//
////    fun<T> addSerializer(clazz:Class<T>, serializer: JsonSerializer<T>){
////        this.add
////    }
////    override fun afterPropertiesSet() {
////        addSerializer(Date::class.java, DateJsonSerializer());
////        addSerializer(LocalDate::class.java, LocalDateJsonSerializer());
////        addSerializer(LocalTime::class.java, LocalTimeJsonSerializer());
////        addSerializer(LocalDateTime::class.java, LocalDateTimeJsonSerializer())
////        addSerializer(java.sql.Timestamp::class.java, TimestampJsonSerializer())
////
////        addDeserializer(Date::class.java, DateJsonDeserializer())
////        addDeserializer(LocalDate::class.java, LocalDateJsonDeserializer())
////        addDeserializer(LocalTime::class.java, LocalTimeJsonDeserializer())
////        addDeserializer(LocalDateTime::class.java, LocalDateTimeJsonDeserializer())
////        addDeserializer(java.sql.Timestamp::class.java, TimestampJsonDeserializer())
////    }
//}
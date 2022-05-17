//package nbcp.component
//
//import com.fasterxml.jackson.annotation.JsonInclude
//import com.fasterxml.jackson.core.JsonParser
//import com.fasterxml.jackson.databind.*
//import com.fasterxml.jackson.databind.module.SimpleModule
//import com.fasterxml.jackson.module.kotlin.registerKotlinModule
//import nbcp.comm.*
//import nbcp.scope.*
//import java.sql.Timestamp
//import java.text.SimpleDateFormat
//import java.time.LocalDate
//import java.time.LocalDateTime
//import java.time.LocalTime
//import java.util.*
//
//abstract class BaseJsonMapper : ObjectMapper() {
//
//    fun <T> addTypeModule(type: Class<T>, ser: JsonSerializer<T>, deser: JsonDeserializer<T>) {
//        var item = SimpleModule(type.name)
//        item.addSerializer(type, ser)
//        this.registerModule(item);
//
//        var item2 = SimpleModule(type.name)
//        item2.addDeserializer(type, deser)
//        this.registerModule(item2)
//    }
//
//
//    fun init() {
//
//        // 设置输出时包含属性的风格
//        this.findAndRegisterModules();
//
//        //这句话会让 kotlin isXXX:Boolean 原样输出。
//        this.registerKotlinModule()
//
//        // 允许单引号、允许不带引号的字段名称
//        this.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
//        this.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
//        this.configure(MapperFeature.USE_STD_BEAN_NAMING, true)
//
//        // 设置输入时忽略在JSON字符串中存在但Java对象实际没有的属性
//        this.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
//
//        this.setSerializationInclusion(JsonInclude.Include.NON_NULL)
//        // 设置时区
//        this.setTimeZone(TimeZone.getTimeZone("GMT+:08:00"))
//
//        this.dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//
//
//        addTypeModule(MyString::class.java, MyStringSerializer(), MyStringDeserializer())
//        addTypeModule(Date::class.java, DateJsonSerializer(), DateJsonDeserializer())
//        addTypeModule(LocalDate::class.java, LocalDateJsonSerializer(), LocalDateJsonDeserializer())
//        addTypeModule(LocalTime::class.java, LocalTimeJsonSerializer(), LocalTimeJsonDeserializer())
//        addTypeModule(
//            LocalDateTime::class.java,
//            LocalDateTimeJsonSerializer(),
//            LocalDateTimeJsonDeserializer()
//        )
//        addTypeModule(Timestamp::class.java, TimestampJsonSerializer(), TimestampJsonDeserializer())
//        addTypeModule(MyRawString::class.java, MyRawStringSerializer(), MyRawStringDeserializer())
//    }
//}
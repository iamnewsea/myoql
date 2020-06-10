package nbcp.comm

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import nbcp.utils.*
import nbcp.comm.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Component
import java.lang.RuntimeException
import java.sql.Timestamp
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by udi on 17-5-23.
 */


//class RawJsonObject(value: String) : MyString(value) {}
//
//@Component
//@DependsOn("myJsonModule")
//class RawJsonSerializer : JsonSerializer<RawJsonObject>(), InitializingBean {
//    override fun serialize(o: RawJsonObject?, j: JsonGenerator, s: SerializerProvider) {
//        if (o == null) {
//            j.writeNull()
//        } else {
//            j.writeRawValue(o.toString())
//        }
//    }
//
//    override fun afterPropertiesSet() {
//        SpringUtil.getBean<DefaultMyJsonMapper>().addSerializer(RawJsonObject::class.java, this)
//    }
//}

//private fun getJsonInstance(getSetStyle: Boolean = false, withNull: Boolean = false): ObjectMapper {
//    return if (getSetStyle && withNull)
//        GetSetWithNullTypeJsonMapper.instance
//    else if (getSetStyle && !withNull)
//        GetSetTypeJsonMapper.instance
//    else if (!getSetStyle && withNull)
//        FieldWithNullTypeJsonMapper.instance
//    else FieldTypeJsonMapper.instance
//}


/**
 * 样式请使用 using(listOf(JsonStyleEnum.FieldStyle)){}
 */
fun <T> T.ToJson(): String {
    if (this is String) return this;

    var styles = scopes.getScopeTypes<JsonStyleEnumScope>()
    var mapper = DefaultMyJsonMapper.get(*styles.toTypedArray())
    if (styles.contains(JsonStyleEnumScope.Pretty)) {
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this) ?: ""
    }

    return mapper.writeValueAsString(this) ?: ""
}

//fun <T> T.ToJsonWithNull(getSetStyle: Boolean = false, pretty: Boolean = false): String {
//    if (this is String) return this;
//
//    if (pretty) {
//        return getJsonInstance(getSetStyle, true).writerWithDefaultPrettyPrinter().writeValueAsString(this) ?: ""
//    }
//    return getJsonInstance(getSetStyle, true).writeValueAsString(this) ?: ""
//}

//如果是 string , 会返回： "123" 这样， 用于 返回的 Json Value
//fun <T> T.ToJsonValue(getSetStyle: Boolean = false, withNull: Boolean = false): String {
//    return getJsonInstance(getSetStyle, withNull).writeValueAsString(this) ?: "null"
//}

fun <T> String.FromJsonWithDefaultValue(collectionClass: Class<T>, getSetStyle: Boolean = false, withNull: Boolean = false): T {
    return this.FromJson<T>(collectionClass, getSetStyle, withNull) ?: collectionClass.newInstance()
}

fun <T> String.FromJson(collectionClass: Class<T>, getSetStyle: Boolean = false, withNull: Boolean = false): T? {
    if (this.isEmpty()) return null

    if (collectionClass == String::class.java) {
        return this as T
    }

    var jsonString = this.RemoveComment().Remove("\r\n", "\n")
    if (jsonString.isEmpty()) {
        return null;
    }

    var ret: T? = null
    try {
        ret = DefaultMyJsonMapper.get().readValue(jsonString, collectionClass)
    } catch (e: Exception) {
        var msg = "Json转换出错！Json数据：${jsonString}\n 类型:${collectionClass.name} \n 错误消息:" + e.message;
        throw RuntimeException(msg, e);
    }
    return ret!!;
}


fun <T> Any.ConvertJson(clazz: Class<T>): T {
    if (clazz.isAssignableFrom(this::class.java)) {
        return this as T;
    }
    return DefaultMyJsonMapper.get().convertValue(this, clazz)
}

inline fun <reified T> String.FromJson(): T? {
    return this.FromJson(T::class.java)
}

inline fun <reified T> String.FromJsonWithDefaultValue(): T {
    return this.FromJsonWithDefaultValue(T::class.java)
}


fun ObjectMapper.setStyle(vararg styles: JsonStyleEnumScope): ObjectMapper {
    // 设置输出时包含属性的风格
    this.findAndRegisterModules();
    this.registerKotlinModule()
    // 允许单引号、允许不带引号的字段名称
    this.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
    this.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
    this.configure(MapperFeature.USE_STD_BEAN_NAMING, true)


    // 设置输入时忽略在JSON字符串中存在但Java对象实际没有的属性
    this.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

//    this.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)

    if (styles.contains(JsonStyleEnumScope.GetSetStyle)) {
        this.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.DEFAULT);
        this.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.DEFAULT);
        this.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.DEFAULT);
        this.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.NONE);
    } else {
        this.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
        this.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        this.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);
        this.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    if (styles.contains(JsonStyleEnumScope.WithNull)) {
//        this.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT)
    } else {
        this.setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }

    // 设置时区
    this.setTimeZone(TimeZone.getTimeZone("GMT+:08:00"))

    this.dateFormat = SimpleDateFormat(styles.toList().getDateFormat());


    //在某些时候，如 mongo.aggregate.group._id 时， null 。
    //默认只序列化 not null 的。

    DefaultMyJsonMapper.sers.forEach {
        this.registerModule(it);
    }

    DefaultMyJsonMapper.desers.forEach {
        this.registerModule(it);
    }

//    if (styles.contains(JsonStyleEnumScope.Pretty)) {
//        this.setDefaultPrettyPrinter(this.serializationConfig.defaultPrettyPrinter)
//    }
    return this;
}

/**
 * 这个方法不准确，应该按 scopes.getLatestScope(JsonStyleEnumScope.DateLocalStyle,JsonStyleEnumScope.DateUtcStyle, JsonStyleEnumScope.DateStandardStyle) ?: JsonStyleEnumScope.DateStandardStyle
 */
private fun List<JsonStyleEnumScope>.getDateFormat(): String {
    if (this.contains(JsonStyleEnumScope.DateUtcStyle)) {
        return "yyyy-MM-dd'T'HH:mm:ss'Z'"
    } else if (this.contains(JsonStyleEnumScope.DateLocalStyle)) {
        return "yyyy/MM/dd HH:mm:ss"
    } else {
        return "yyyy-MM-dd HH:mm:ss";
    }
}

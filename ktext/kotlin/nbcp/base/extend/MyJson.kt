package nbcp.base.extend

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.*
import nbcp.base.utils.SpringUtil
import nbcp.comm.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Component
import java.lang.RuntimeException
import java.sql.Timestamp


/**
 * Created by udi on 17-5-23.
 */


class RawJsonObject(value: String) : MyString(value) {}

@Component
@DependsOn("myJsonModule")
class RawJsonSerializer : JsonSerializer<RawJsonObject>(), InitializingBean {
    override fun serialize(o: RawJsonObject?, j: JsonGenerator, s: SerializerProvider) {
        if (o == null) {
            j.writeNull()
        } else {
            j.writeRawValue(o.toString())
        }
    }

    override fun afterPropertiesSet() {
        SpringUtil.getBean<MyJsonModule>().addSerializer(RawJsonObject::class.java, this)
    }
}

private fun getJsonInstance(getSetStyle: Boolean = false, withNull: Boolean = false): ObjectMapper {
    return if (getSetStyle && withNull)
        GetSetWithNullTypeJsonMapper.instance
    else if (getSetStyle && !withNull)
        GetSetTypeJsonMapper.instance
    else if (!getSetStyle && withNull)
        FieldWithNullTypeJsonMapper.instance
    else FieldTypeJsonMapper.instance
}

/**
 * @param getSetStyle 使用 Field 还是 GetSet 序列化。默认使用 Field
 * @param withNull: 序列化时，是否序列化 null 值 。 默认不序列化
 */
fun <T> T.ToJson(getSetStyle: Boolean = false, withNull: Boolean = false): String {
    if (this is String) return this;

    return getJsonInstance(getSetStyle, withNull).writeValueAsString(this) ?: ""
}

fun <T> T.ToJsonWithNull(getSetStyle: Boolean = false): String {
    if (this is String) return this;

    return getJsonInstance(getSetStyle, true).writeValueAsString(this) ?: ""
}

//如果是 string , 会返回： "123" 这样， 用于 返回的 Json Value
fun <T> T.ToJsonValue(getSetStyle: Boolean = false, withNull: Boolean = false): String {
    return getJsonInstance(getSetStyle, withNull).writeValueAsString(this) ?: "null"
}

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

    var mapper: JsonBaseObjectMapper;

    if (getSetStyle) {
        if (withNull) {
            mapper = GetSetWithNullTypeJsonMapper.instance;
        } else {
            mapper = GetSetTypeJsonMapper.instance;
        }
    } else if (withNull) {
        mapper = FieldWithNullTypeJsonMapper.instance;
    } else {
        mapper = FieldTypeJsonMapper.instance;
    }

    var ret: T? = null
    try {
        ret = mapper.readValue(jsonString, collectionClass)
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
    return FieldTypeJsonMapper.instance.convertValue(this, clazz)
}

inline fun <reified T> String.FromJson(): T? {
    return this.FromJson(T::class.java)
}

inline fun <reified T> String.FromJsonWithDefaultValue(): T {
    return this.FromJsonWithDefaultValue(T::class.java)
}
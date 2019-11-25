package nbcp.base.extend

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.json.PackageVersion
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import nbcp.base.extend.FieldTypeJsonMapper.Companion.logger
import nbcp.base.utils.CodeUtil
import nbcp.base.utils.SpringUtil
import java.io.IOException
import java.text.DateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


/**
 * Created by udi on 17-5-23.
 */

class RawJsonObject(value: String) : MyString(value) {}

@Component
class RawJsonSerializer : JsonSerializer<RawJsonObject>() {
    override fun serialize(o: RawJsonObject?, j: JsonGenerator, s: SerializerProvider) {
        if (o == null) {
            j.writeNull()
        } else {
            j.writeRawValue(o.toString())
        }
    }

    override fun handledType(): Class<RawJsonObject> {
        return RawJsonObject::class.java
    }
}


fun <T> T.ToJson(getSetStyle: Boolean = false): String {
    if (this is String) return this;

    var instance: ObjectMapper = if (getSetStyle) GetSetTypeJsonMapper.instance else FieldTypeJsonMapper.instance

    return FieldTypeJsonMapper.instance.writeValueAsString(this) ?: ""
}

//如果是 string , 会返回： "123" 这样， 用于 返回的 Json Value
fun <T> T.ToJsonValue(): String {
    return FieldTypeJsonMapper.instance.writeValueAsString(this) ?: "null"
}


fun <T> String.FromJson(collectionClass: Class<T>): T {
    if (collectionClass == String::class.java) {
        return this as T
    }

    var jsonString = this.RemoveComment().Remove("\r\n", "\n")
    if (jsonString.isEmpty()) {
        return collectionClass.newInstance();
    }

    var ret: T? = null
    try {
        ret = FieldTypeJsonMapper.instance.readValue(jsonString, collectionClass)
    } catch (e: Exception) {
        var msg = "Json转换出错！Json数据：${jsonString}\n 类型:${collectionClass.name} \n 错误消息:" + e.message;
        logger.error(msg)
        throw e;
    }
    return ret!!;
}


fun <T> Any.ConvertJson(clazz: Class<T>): T {
    if (clazz.isAssignableFrom(this::class.java)) {
        return this as T;
    }
    return FieldTypeJsonMapper.instance.convertValue(this, clazz)
}

inline fun <reified T> String.FromJson(): T {
    return this.FromJson(T::class.java)
}

@file:JvmName("MyJson")
@file:JvmMultifileClass

package nbcp.comm

import java.lang.RuntimeException
import nbcp.scope.*

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
 * 样式请使用 usingScope(listOf(JsonStyleEnum.FieldStyle)){}
 */
@JvmOverloads
fun <T> T.ToJson(style: JsonSceneEnumScope? = null): String {
    if (this is String) return this;

    var mapper = style.getJsonMapper();

    var styles = scopes.getScopeTypes<JsonStyleEnumScope>()
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

@JvmOverloads
fun <T> String.FromJsonWithDefaultValue(
    collectionClass: Class<T>,
    style: JsonSceneEnumScope? = null
): T {
    return this.FromJson<T>(collectionClass, style) ?: collectionClass.newInstance()
}


@JvmOverloads
fun <T> String.FromJson(collectionClass: Class<T>, style: JsonSceneEnumScope? = null): T? {
    if (this.isEmpty()) return null

    if (collectionClass == String::class.java) {
        return this as T
    }

    var jsonString = this
    if (jsonString.isEmpty()) {
        return null;
    }

    var mapper = style.getJsonMapper();

    var ret: T?
    try {
        ret = mapper.readValue(jsonString, collectionClass)
    } catch (e: Exception) {
        var msg = "Json转换出错！Json数据：${jsonString}\n 类型:${collectionClass.name} \n 错误消息:" + e.message;
        throw RuntimeException(msg, e);
    }
    return ret!!;
}

@JvmOverloads
fun <T> Any.ConvertJson(clazz: Class<T>,style: JsonSceneEnumScope? = null): T {
    if (clazz.isAssignableFrom(this::class.java)) {
        return this as T;
    }
    return style.getJsonMapper().convertValue(this, clazz)
}


inline fun <reified T> String.FromJson(): T? {
    return this.FromJson(T::class.java)
}

inline fun <reified T> String.FromJsonWithDefaultValue(): T {
    return this.FromJsonWithDefaultValue(T::class.java)
}


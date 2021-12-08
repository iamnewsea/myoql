@file:JvmName("MyJson")
@file:JvmMultifileClass

package nbcp.comm

import nbcp.scope.*
import kotlin.RuntimeException
import kotlin.reflect.KClass

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

/**
 * 转为List
 * @param componentClass :组件类型
 */
@JvmOverloads
fun <T> String.FromListJson(componentClass: Class<T>, style: JsonSceneEnumScope? = null): List<T> {
    if (this.isEmpty()) return listOf()
    val mapper = style.getJsonMapper();

    try {
        var t = mapper.getTypeFactory().constructParametricType(List::class.java, componentClass);
        return mapper.readValue<List<T>>(this, t) ?: listOf<T>()
    } catch (e: Exception) {
        var msg = "Json转换出错！Json数据：${this}\n 类型:${componentClass.name} \n 错误消息:" + e.message;
        throw RuntimeException(msg, e);
    }
}


fun <T : Any> String.FromJson(clazz: KClass<T>, style: JsonSceneEnumScope? = null): T? {
    return this.FromJson(clazz.java, style)
}

@JvmOverloads
fun <T> String.FromJson(clazz: Class<T>, style: JsonSceneEnumScope? = null): T? {
    if (this.isEmpty()) return null

    if (clazz == String::class.java) {
        return this as T
    }

    val mapper = style.getJsonMapper();

    try {
        return mapper.readValue(this, clazz)
    } catch (e: Exception) {
        var msg = "Json转换出错！Json数据：${this}\n 类型:${clazz.name} \n 错误消息:" + e.message;
        throw RuntimeException(msg, e);
    }
}


fun <T : Any> Any.ConvertJson(clazz: KClass<out T>, style: JsonSceneEnumScope? = null): T {
    return this.ConvertJson(clazz.java, style)
}

/**
 * 从 ConvertType 做为入口。 为避免死循环，禁止从ConvertJson调用 ConvertType。
 */
@JvmOverloads
fun <T> Any.ConvertJson(clazz: Class<out T>, style: JsonSceneEnumScope? = null): T {
    if (clazz.isAssignableFrom(this::class.java)) {
        return this as T;
    }

    //如果是 String，转
    if (this is String) {
        return this.FromJson(clazz, style) ?: throw RuntimeException("转换Json出错")
    }
    return style.getJsonMapper().convertValue(this, clazz)
}


fun <T : Any> Any.ConvertListJson(componentClass: KClass<out T>, style: JsonSceneEnumScope? = null): List<T> {
    return this.ConvertListJson(componentClass.java, style)
}

/**
 * 转换为List
 * @param componentClass: 组件类型
 */
@JvmOverloads
fun <T> Any.ConvertListJson(componentClass: Class<out T>, style: JsonSceneEnumScope? = null): List<T> {
    val mapper = style.getJsonMapper();
    val t = mapper.getTypeFactory().constructParametricType(List::class.java, componentClass);
    return mapper.convertValue(this, t)
}


inline fun <reified T> String.FromJson(): T? {
    return this.FromJson(T::class.java)
}

inline fun <reified T> String.FromJsonWithDefaultValue(): T {
    return this.FromJsonWithDefaultValue(T::class.java)
}


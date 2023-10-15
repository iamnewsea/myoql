@file:JvmName("MyJson")
@file:JvmMultifileClass

package nbcp.base.extend

import nbcp.base.enums.JsonSceneScopeEnum
import nbcp.base.enums.JsonStyleScopeEnum
import nbcp.base.extend.AsString
import nbcp.base.extend.scopes
import nbcp.base.extend.usingScope
import nbcp.base.scope.getJsonMapper
import nbcp.base.utils.ReflectUtil
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


@JvmOverloads
fun <T> T.ToJson(style: JsonSceneScopeEnum? = null): String {
    return this.ToJson(style, *arrayOf<JsonStyleScopeEnum>())
}

fun <T> T.ToJson(vararg jsonScopes: JsonStyleScopeEnum): String {
    return this.ToJson(null, *jsonScopes)
}

/**
 * 样式请使用 usingScope(listOf(JsonStyleEnum.FieldStyle)){}
 */
fun <T> T.ToJson(style: JsonSceneScopeEnum?, vararg jsonScopes: JsonStyleScopeEnum): String {
    if (this is String) return this;

    usingScope(jsonScopes) {
        var mapper = style.getJsonMapper();

        var styles = scopes.getScopeTypes<JsonStyleScopeEnum>()
        if (styles.contains(JsonStyleScopeEnum.PRETTY)) {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this) ?: ""
        }

        return mapper.writeValueAsString(this) ?: ""
    }
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
    style: JsonSceneScopeEnum? = null
): T {
    return this.FromJson<T>(collectionClass, style) ?: collectionClass.newInstance()
}

/**
 * 转为List
 * @param componentClass :组件类型
 */
@JvmOverloads
fun <T> String.FromListJson(componentClass: Class<T>, style: JsonSceneScopeEnum? = null): List<T> {
    if (this.isEmpty()) return listOf()
    val mapper = style.getJsonMapper();

    try {
        var t = mapper.getTypeFactory().constructParametricType(ArrayList::class.java, componentClass);
        return mapper.readValue<List<T>>(this, t) ?: listOf<T>()
    } catch (e: Exception) {
        var msg = "Json转换出错！Json数据：${this}\n 类型:${componentClass.name} \n 错误消息:" + e.message;
        throw RuntimeException(msg, e);
    }
}

/**
 * 反序列化泛型类
 */
@JvmOverloads
fun <T> String.FromGenericJson(type: Class<T>, vararg genericClasses: Class<*>, style: JsonSceneScopeEnum? = null): T? {
    if (this.isEmpty()) return null
    val mapper = style.getJsonMapper();

    try {
        var t = mapper.getTypeFactory().constructParametricType(type, *genericClasses);
        return mapper.readValue(this, t)
    } catch (e: Exception) {
        var msg = "Json转换出错！Json数据：${this}\n 类型:${type.name} \n 错误消息:" + e.message;
        throw RuntimeException(msg, e);
    }
}


fun <T : Any> String.FromJson(type: KClass<T>, style: JsonSceneScopeEnum? = null): T? {
    return this.FromJson(type.java, style)
}

@JvmOverloads
fun <T> String.FromJson(type: Class<T>, style: JsonSceneScopeEnum? = null): T? {
    return this.FromJsonText(type, style)
}


@JvmOverloads
fun <T> String.FromJsonText(type: Class<T>, style: JsonSceneScopeEnum? = null): T? {
    if (this.isEmpty()) return null

    if (type == String::class.java) {
        return this as T
    }

    val mapper = style.getJsonMapper();

    try {
        return mapper.readValue(this, type)
    } catch (e: Exception) {
        var msg = "Json转换出错！Json数据：${this}\n 类型:${type.name} \n 错误消息:" + e.message;
        throw RuntimeException(msg, e);
    }
}


fun <T : Any> Any.ConvertJson(type: KClass<out T>, style: JsonSceneScopeEnum? = null): T {
    return this.ConvertJson(type.java, style)
}

/**
 * 从 ConvertType 做为入口。 为避免死循环，禁止从ConvertJson调用 ConvertType。
 */
@JvmOverloads
fun <T> Any.ConvertJson(type: Class<out T>, style: JsonSceneScopeEnum? = null): T {
//    if (clazz.isAssignableFrom(this::class.java)) {
//        return this as T;
//    }

    //如果是 String，转
    if (this is String) {
        return this.FromJson(type, style) ?: throw RuntimeException("转换Json出错")
    } else if (this is Map<*, *>) {
        if (Map::class.java.isAssignableFrom(type) == false) {
            //处理 a.b.c = "10
            this.keys
                .filter {
                    var key = it.AsString();
                    return@filter key.contains(".") || key.contains("[]")
                }
                .forEach { it ->
                    var key = it.AsString();
                    ReflectUtil.setValueByWbsPath(this, key, value = this.get(key))
                }
        }
    }

    return style.getJsonMapper().convertValue(this, type)
}


fun <T : Any> Any.ConvertListJson(componentClass: KClass<out T>, style: JsonSceneScopeEnum? = null): List<T> {
    return this.ConvertListJson(componentClass.java, style)
}

/**
 * 转换为List
 * @param componentClass: 组件类型
 */
@JvmOverloads
fun <T> Any.ConvertListJson(componentClass: Class<out T>, style: JsonSceneScopeEnum? = null): List<T> {
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


fun Array<out JsonStyleScopeEnum>.getDateFormat(): String = this.toList().getDateFormat()

/**
 */
fun Collection<JsonStyleScopeEnum>.getDateFormat(): String {
    if (this.contains(JsonStyleScopeEnum.DATE_UTC_STYLE)) {
        return "yyyy-MM-dd'T'HH:mm:ss'Z'"
    } else if (this.contains(JsonStyleScopeEnum.DATE_LOCAL_STYLE)) {
        return "yyyy/MM/dd HH:mm:ss"
    } else {
        return "yyyy-MM-dd HH:mm:ss";
    }
}


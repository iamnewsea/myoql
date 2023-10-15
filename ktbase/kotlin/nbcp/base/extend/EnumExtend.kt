@file:JvmName("MyHelper")
@file:JvmMultifileClass

package nbcp.base.extend

import com.fasterxml.jackson.annotation.JsonValue
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType


/**
 * 获取枚举类的所有成员
 */
@JvmOverloads
fun <T> Class<T>.GetEnumList(values: String = ""): List<T> {
    if (this.isEnum == false) return listOf()

    var list = this.enumConstants

    if (values.HasValue) {
        return values.split(",")
            .map { v -> list.find { it.toString() == v } }
            .filterNotNull()
    }

    return list.toList()
}

/**
 * 获取枚举类的数字类型的字段。
 */
fun <T> Class<T>.GetEnumNumberField(): Field? {
    if (this.isEnum == false) return null


    this.declaredFields
        .filter {
            it.IsPrivate && Modifier.isFinal(it.modifiers) && it.type.IsNumberType
        }
        .let { ret_fields ->
            if (ret_fields.size == 1) {
                var ret = ret_fields.first();
                ret.isAccessible = true;
                return ret;
            }
        }


    return null;
}

fun <T : Enum<T>> Class<T>.GetEnumJsonValueField(): Field? {

    var ret = this.declaredFields.firstOrNull { !it.IsStatic && it.getAnnotation(JsonValue::class.java) != null };

    if (ret != null) {
        ret.isAccessible = true;
    }

    return ret;
}

fun <T : Enum<*>> T.GetEnumJsonValueValue(): String {

    var f = (this.javaClass as Class<out Enum<*>>).GetEnumJsonValueField();
    if (f == null) {
        return this.toString();
    }

    return f.get(this).AsString();
}

/**
 * 找枚举
 */
fun Class<*>.findAllEnum(): Set<Class<Enum<*>>> {
    return findAllEnum(this, mutableSetOf(), 8);
}

private fun findAllEnum(type: Class<*>, loaded: MutableSet<Class<*>>, deepth: Int): Set<Class<Enum<*>>> {
    var ret = mutableSetOf<Class<Enum<*>>>();
    if (deepth == 0) {
        return setOf()
    }

    if (loaded.contains(type)) {
        return setOf()
    }
    loaded.add(type);

    for (field in type.AllFields) {
        if (field.type.isEnum) {
            ret.add(field.type as Class<Enum<*>>)
            continue;
        }

        if (field.type.isArray) {
            var comType = field.type.componentType;
            ret.addAll(findAllEnum(comType, loaded, deepth - 1))
            continue;
        }

        if (field.type.IsCollectionType) {
            var gas = (field.genericType as ParameterizedType).actualTypeArguments;
            for (gas1 in gas) {
                if (gas1 is Class<*>) {
                    ret.addAll(findAllEnum(gas1, loaded, deepth - 1))
                }
            }
            continue;
        }

        if (field.type.IsSimpleType()) {
            continue;
        }

        ret.addAll(findAllEnum(field.type, loaded, deepth - 1))
    }

    return ret;
}
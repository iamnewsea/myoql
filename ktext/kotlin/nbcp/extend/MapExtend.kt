@file:JvmName("MyHelper")
@file:JvmMultifileClass

package nbcp.comm

import nbcp.utils.JsUtil
import nbcp.utils.MyUtil


/**
 * 先删除,再插入的方式修改,顺序会变
 */
fun <V> LinkedHashMap<String, V>.RenameKey(oldKey: String, newKey: String) {
    var index = this.keys.indexOf(oldKey);
    if (index < 0) {
        throw RuntimeException("找不到Key")
    }

    var value = this.get(oldKey)!!;
    this.remove(oldKey);
    this.put(newKey, value);
}

inline fun <reified K, reified V, reified RK, reified RV> Map<K, V>.ToMap(
    keyAct: ((Map.Entry<K, V>) -> RK),
    valueAct: ((Map.Entry<K, V>) -> RV)
): LinkedHashMap<RK, RV> {
    var map = linkedMapOf<RK, RV>()
    this.forEach {
        map[keyAct(it)] = valueAct(it);
    }
    return map;
}

inline fun <reified T, reified RK, reified RV> Collection<T>.ToMap(
    keyAct: ((T) -> RK),
    valueAct: ((T) -> RV)
): LinkedHashMap<RK, RV> {
    var map = linkedMapOf<RK, RV>()
    this.forEach {
        map[keyAct(it)] = valueAct(it);
    }
    return map;
}


fun <V> MutableMap<String, V>.onlyHoldKeys(keys: Set<String>) {
    this.keys.minus(keys.toTypedArray()).forEach {
        this.remove(it)
    }
}

inline fun <reified T> Map<String, *>.getTypeValue(vararg keys: String): T? {
    var ret = MyUtil.getPathValue(this, *keys);
    if (ret === null) return null;
    if (ret is T) return ret;
    return null;
}

fun Map<*, *>.getStringValue(vararg keys: String): String? {
    var v = MyUtil.getPathValue(this, *keys)
    if (v == null) return null;
    var v_type = v::class.java;
    if (v_type.isArray) {
        return (v as Array<Any>).map { it.AsString() }.joinToString(",")
    } else if (v_type.IsCollectionType) {
        return (v as Collection<Any>).map { it.AsString() }.joinToString(",")
    }
    return v.toString()
}

fun Map<*, *>.getIntValue(vararg keys: String): Int {
    var v = MyUtil.getPathValue(this, *keys)
    if (v == null) return 0;
    return v.AsInt()
}

//------------------

private fun get_array_querys(list: Collection<Any?>): List<String> {
    return list.map { value ->
        if (value == null) return@map arrayOf<String>();

        var type = value::class.java;
        if (type.IsSimpleType() == false) {
            if (type.isArray) {
                return@map get_array_querys((value as Array<*>).toList())
                    .map { "[]=" + it }
                    .toTypedArray()
            } else if (type.IsCollectionType) {
                return@map get_array_querys((value as Collection<*>))
                    .map { "[]=" + it }
                    .toTypedArray()
            } else if (type.IsMapType) {
                return@map get_map_querys((value as Map<String, *>))
                    .map { "[]=" + it }
                    .toTypedArray()
            }
            return@map get_map_querys(value.ConvertJson(JsonMap::class.java))
                .map { "[]=" + it }
                .toTypedArray()
        }

        return@map arrayOf(JsUtil.encodeURIComponent(value.toString()))
    }
        .Unwind()
        .filter { it.HasValue }
}

private fun get_map_querys(map: Map<String, *>): List<String> {
    return map.map {
        var key = it.key;
        var value = it.value;
        if (value == null) {
            return@map arrayOf<String>();
        }

        var type = value::class.java;

        if (type.IsSimpleType() == false) {
            if (type.isArray) {
                return@map get_array_querys((value as Array<*>).toList())
                    .map { key + it }
                    .toTypedArray()
            } else if (type.IsCollectionType) {
                return@map get_array_querys((value as Collection<*>))
                    .map { key + it }
                    .toTypedArray()
            } else if (type.IsMapType) {
                return@map get_map_querys((value as Map<String, *>))
                    .map { key + "." + it }
                    .toTypedArray()
            }
            return@map get_map_querys(value.ConvertJson(JsonMap::class.java))
                .map { key + "." + it }
                .toTypedArray()
        }

        return@map arrayOf(key + "=" + JsUtil.encodeURIComponent(value.toString()))
    }
        .Unwind()
        .filter { it.HasValue }
}


/**
 * 返回Url中参数,不带问号, 复杂情况可能会出错。 慎用！
 */
fun Map<String, *>.toUrlQuery(): String {
    return get_map_querys(this).joinToString("&")
}


fun Map<String, Any?>.getKeyByValue(value: Any): String? {
    return this.entries.firstOrNull { it.value == value }?.key
}
@file:JvmName("MyHelper")
@file:JvmMultifileClass

package nbcp.comm

import nbcp.utils.JsUtil


//顺序会变
fun <V> LinkedHashMap<String, V>.RenameKey(oldKey: String, newKey: String) {
    var index = this.keys.indexOf(oldKey);
    if (index < 0) {
        throw RuntimeException("找不到Key")
    }

    var value = this.get(oldKey)!!;
    this.remove(oldKey);
    this.put(newKey, value);

//    var other = LinkedHashMap<String, V>();
//
//    for (i in this.keys.indices) {
//        var k = this.keys.elementAt(i);
//        if (k == oldKey) {
//            var value = this[oldKey];
//            for (j in i + 1..this.size - 1) {
//                k = this.keys.elementAt(j);
//                other.put(k, this[k]!!);
//            }
//
//            for (j in i..this.size - 1) {
//                k = this.keys.elementAt(i + 1);
//                this.remove(k)
//            }
//
//            this.put(newKey, value!!);
//
//            for (j in other.keys.indices) {
//                k = other.keys.elementAt(j);
//                this.put(k, other[k]!!);
//            }
//            break;
//        }
//    }
}

inline fun <reified K, reified V, reified RK, reified RV> Map<K, V>.ToMap(keyAct: ((Map.Entry<K, V>) -> RK), valueAct: ((Map.Entry<K, V>) -> RV)): LinkedHashMap<RK, RV> {
    var map = linkedMapOf<RK, RV>()
    this.forEach {
        map[keyAct(it)] = valueAct(it);
    }
    return map;
}

inline fun <reified T, reified RK, reified RV> List<T>.ToMap(keyAct: ((T) -> RK), valueAct: ((T) -> RV)): LinkedHashMap<RK, RV> {
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
    var ret = this.getPathValue(*keys);
    if (ret === null) return null;
    if (ret is T) return ret;
    return null;
}

/**
 * 通过 path 获取 value,每级返回的值必须是 Map<String,V> 否则返回 null
 * @param key:
 */
fun Map<String, *>.getPathValue(vararg keys: String): Any? {
    if (keys.any() == false) return null;
    var key = keys.first();
    var v = this.get(key)
    if (v == null) return null;

    var left_keys = keys.Slice(1);
    if (left_keys.any() == false) return v;

    if (v is Map<*, *>) {
        return (v as Map<String, *>).getPathValue(*left_keys.toTypedArray())
    }
    return v.toString()
}

fun Map<String, *>.getStringValue(vararg keys: String): String? {
    var v = this.getPathValue(*keys)
    if (v == null) return null;
    var v_type = v::class.java;
    if (v_type.isArray) {
        return (v as Array<Any>).map { it.AsString() }.joinToString(",")
    } else if (List::class.java.isAssignableFrom(v_type)) {
        return (v as List<Any>).map { it.AsString() }.joinToString(",")
    }
    return v.toString()
}

fun Map<String, *>.getIntValue(vararg keys: String): Int {
    var v = getPathValue(*keys)
    if (v == null) return 0;
    return v.AsInt()
}

//------------------


private fun get_array_querys(list: List<Any?>): List<String> {
    return list.map { value ->
        if (value == null) return@map arrayOf<String>();

        var type = value::class.java;
        if (type.IsSimpleType() == false) {
            if (type.isArray) {
                return@map get_array_querys((value as Array<*>).toList())
                        .map { "[]=" + it }
                        .toTypedArray()
            } else if (type.IsListType()) {
                return@map get_array_querys((value as List<*>))
                        .map { "[]=" + it }
                        .toTypedArray()
            } else if (type.IsMapType()) {
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
            } else if (type.IsListType()) {
                return@map get_array_querys((value as List<*>))
                        .map { key + it }
                        .toTypedArray()
            } else if (type.IsMapType()) {
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
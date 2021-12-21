@file:JvmName("MyHelper")
@file:JvmMultifileClass

package nbcp.comm

import nbcp.utils.JsUtil
import nbcp.utils.MyUtil


//fun <V> LinkedHashMap<String, V>.RenameKey(oldKey: String, newKey: String) {
//    var index = this.keys.indexOf(oldKey);
//    if (index < 0) {
//        throw RuntimeException("找不到Key")
//    }
//
//    var value = this.get(oldKey)!!;
//    this.remove(oldKey);
//    this.put(newKey, value);
//}

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

/**
 * 多层级获取属性值
 */
inline fun <reified T> Map<String, *>.getTypeValue(vararg keys: String, ignoreCase: Boolean = false): T? {
    var ret = MyUtil.getValueByWbsPath(this, *keys, ignoreCase = ignoreCase);
    if (ret === null) return null;
    if (ret is T) return ret;
    return null;
}

/**
 * 多层级获取属性值
 */
fun Map<*, *>.getStringValue(vararg keys: String, ignoreCase: Boolean = false): String? {
    var v = MyUtil.getValueByWbsPath(this, *keys, ignoreCase = ignoreCase)
    if (v == null) return null;
//    var v_type = v::class.java;
    if (v is Array<*>) {
        return v.map { it.AsString() }.joinToString(",")
    } else if (v is Collection<*>) {
        return v.map { it.AsString() }.joinToString(",")
    }
    return v.toString()
}

/**
 * 多层级获取属性值
 */
fun Map<*, *>.getIntValue(vararg keys: String, ignoreCase: Boolean = false): Int {
    var v = MyUtil.getValueByWbsPath(this, *keys, ignoreCase = ignoreCase)
    if (v == null) return 0;
    return v.AsInt()
}

/**
 * 多层级设置值
 */
fun  Map<String, *>.setValueByWbsPath(
    vararg keys: String,
    ignoreCase: Boolean = false,
    value: Any?
): Boolean {
    return MyUtil.setValueByWbsPath(this, *keys, ignoreCase = ignoreCase, value = value);
}


fun  Map<String, *>.getValueByWbsPath(
    vararg keys: String,
    ignoreCase: Boolean = false
): Any? {
    return MyUtil.getValueByWbsPath(this, *keys, ignoreCase = ignoreCase );
}
//------------------

private fun get_array_querys(list: Collection<Any?>): List<String> {
    return list.map { value ->
        if (value == null) return@map listOf<String>();

        var type = value::class.java;
        if (type.IsSimpleType() == false) {
            if (type.isArray) {
                return@map get_array_querys((value as Array<*>).toList())
                    .map { "[]=" + it }
            } else if (value is Collection<*>) {
                return@map get_array_querys(value)
                    .map { "[]=" + it }
            } else if (value is Map<*, *>) {
                return@map get_map_querys((value as Map<String, *>))
                    .map { "[]=" + it }
            }
            return@map get_map_querys(value.ConvertJson(JsonMap::class.java))
                .map { "[]=" + it }
        }

        return@map listOf(JsUtil.encodeURIComponent(value.toString()))
    }
        .Unwind()
        .filter { it.HasValue }
}

private fun get_map_querys(map: Map<String, *>): List<String> {
    return map.map {
        var key = it.key;
        var value = it.value;
        if (value == null) {
            return@map listOf<String>();
        }

        var type = value::class.java;

        if (type.IsSimpleType() == false) {
            if (type.isArray) {
                return@map get_array_querys((value as Array<*>).toList())
                    .map { key + it }
            } else if (type.IsCollectionType) {
                return@map get_array_querys((value as Collection<*>))
                    .map { key + it }
            } else if (type.IsMapType) {
                return@map get_map_querys((value as Map<String, *>))
                    .map { key + "." + it }
            }
            return@map get_map_querys(value.ConvertJson(JsonMap::class.java))
                .map { key + "." + it }
        }

        return@map listOf(key + "=" + JsUtil.encodeURIComponent(value.toString()))
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

fun MutableMap<String, Any?>.removeKeys(vararg key: String) {
    key.forEach {
        this.remove(it);
    }
}

/**
 * 判断数据是否相同， 不区分顺序。只关心内容，对值进行简单比较
 */
fun Map<*, *>.EqualMapContent(value: Map<*, *>, compare: ((Any?, Any?) -> Boolean)? = null): Boolean {
    if (this.size == 0 && value.size == 0) return true;
    else if (this.size == 0) return false;
    else if (value.size == 0) return false;

    if (this.size != value.size) return false;

    if (this.keys.EqualArrayContent(value.keys) == false) return false;

    if (this.all {
            var value1 = it.value;
            var value2 = value.get(it.key);

            if (value1 is Array<*> && value2 is Array<*>) {
                return@all value1.EqualArrayContent(value2);
            }
            if (value1 is Collection<*> && value2 is Collection<*>) {
                return@all value1.EqualArrayContent(value2);
            }
            if (value1 is Map<*, *> && value2 is Map<*, *>) {
                return@all value1.EqualMapContent(value2);
            }

            if (compare != null) {
                return@all compare(value1, value2);
            }
            return@all it.value == value2;
        } == false) return false;

    return true;
}
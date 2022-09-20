@file:JvmName("MyHelper")
@file:JvmMultifileClass

package nbcp.comm

import nbcp.db.JsonKeyValuePair
import nbcp.db.KeyValueString
import nbcp.utils.JsUtil
import nbcp.utils.MyUtil
import java.util.*
import kotlin.collections.LinkedHashMap


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


/**
 * 1. 不区分大小写。
 * 2. 兼容 KebabCase
 */
fun Map<*, *>.findParameterKey(key: String): Any? {
    var ret = this.getByIgnoreCaseKey(key);
    if (ret != null) {
        return ret;
    }

    //兼容查询
    if (MyUtil.isKebabCase(key)) {
        var key2 = MyUtil.getSmallCamelCase(key);
        if (key != key2) {
            ret = this.getByIgnoreCaseKey(key2);
        }

        return ret;
    }

    var kKey = MyUtil.getKebabCase(key);
    if (kKey != key) {
        ret = this.getByIgnoreCaseKey(kKey);
    }
    return ret;
}

/**
 * 忽略大小写获取键值
 */
fun Map<*, *>.getByIgnoreCaseKey(key: String): Any? {
    var key2 = this.keys.firstOrNull { it basicSame key };
    if (key2 == null) {
        return null
    }
    return this[key2];
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


/**
 * 移除项
 */
fun <K, V> MutableMap<K, V>.removeAll(key: (K) -> Boolean): MutableMap<K, V> {
    this.keys.filter(key).forEach { this.remove(it) }
    return this;
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
fun Map<String, *>.setValueByWbsPath(
    vararg keys: String,
    value: Any?,
    ignoreCase: Boolean = false
): Boolean {
    return MyUtil.setValueByWbsPath(this, *keys, value = value, ignoreCase = ignoreCase);
}


fun Map<String, *>.getValueByWbsPath(
    vararg keys: String,
    ignoreCase: Boolean = false
): Any? {
    return MyUtil.getValueByWbsPath(this, *keys, ignoreCase = ignoreCase);
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


/**
 * 仅移除最后一个Path项
 */
fun MutableMap<String, Any?>.removeByWbsPath(vararg keys: String): Boolean {

    var unwindKeys = keys
        .map { it.split('.') }
        .Unwind()
        .map { it.trim() }
        .filter { it.HasValue }
        .toTypedArray();

    if (unwindKeys.any() == false) return false;
    if (unwindKeys.size == 1) {
        return this.remove(unwindKeys.first()) != null
    }

    var target = this.getValueByWbsPath(*unwindKeys.ArraySlice(0, -1).toTypedArray())
    if (target == null) return false;
    if (target is MutableMap<*, *> == false) {
        throw RuntimeException("移除的对象不是Map,是:${target::class.java.name} ,path: ${unwindKeys.joinToString(".")}")
    }
    return target.remove(unwindKeys.last()) != null
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
                return@all value1.toList().EqualArrayContent(value2.toList());
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

fun Map<String, Any?>.ToProperties(): Properties {
    var ret = Properties();
    ret.putAll(this)
    return ret;
}

private fun Collection<Any?>.ToListKv(): List<JsonKeyValuePair<Any>> {
    var ret = mutableListOf<JsonKeyValuePair<Any>>()
    this.forEachIndexed { index, value ->
        if (value == null) {
            return@forEachIndexed
        }

        var v_type = value::class.java;
        if (v_type.IsSimpleType()) {
            ret.add(JsonKeyValuePair("[${index}]", value))
            return@forEachIndexed
        }

        if (v_type.IsCollectionType) {
            var list3 = (value as Collection<Any?>).ToListKv();
            list3.forEach {
                it.key = "[${index}]" + it.key;
            }
            ret.addAll(list3);
            return@forEachIndexed
        }

        if (v_type.isArray) {
            var list3 = (value as Array<Any?>).toList().ToListKv();
            list3.forEach {
                it.key = "[${index}]" + it.key;
            }
            ret.addAll(list3);
            return@forEachIndexed
        }

        var list4 = (value.ConvertType(Map::class.java) as Map<String, Any?>).ToListKv()
        list4.forEach {
            it.key = "[${index}]." + it.key;
        }
        ret.addAll(list4);

        return@forEachIndexed
    }

    return ret;
}

fun Map<String, Any?>.ToListKv(prefix: String = ""): List<JsonKeyValuePair<Any>> {
    var list = mutableListOf<JsonKeyValuePair<Any>>()

    this.keys.forEach { key ->
        var value = this.get(key);
        if (value == null) {
            return@forEach
        }

        var v_type = value::class.java;
        if (v_type.IsSimpleType()) {
            list.add(JsonKeyValuePair(key, value))

            return@forEach
        }

        if (v_type.IsCollectionType) {
            var list2 = (value as Collection<Any?>).ToListKv()
            list2.forEach {
                it.key = key + it.key;
            }

            list.addAll(list2);
            return@forEach
        }

        if (v_type.isArray) {
            var list2 = (value as Array<Any?>).toList().ToListKv()
            list2.forEach {
                it.key = key + it.key;
            }

            list.addAll(list2);
            return@forEach
        }

        var v1_map = value.ConvertType(Map::class.java) as Map<String, Any?>

        var v1_kvs = v1_map.ToListKv(key);

        list.addAll(v1_kvs)
    }

    if (prefix.HasValue) {
        list.forEach {
            it.key = prefix + "." + it.key
        }
    }

    return list
}

/**
 * 深度合并
 */
fun Map<String, Any?>.deepJoin(map2: Map<String, Any?>): Map<String, Any?> {

    var ret = JsonMap()
    (this.keys - map2.keys).forEach { key ->
        val value = this.get(key);
        ret.put(key, value);
    }


    (map2.keys - this.keys).forEach { key ->
        val value = map2.get(key);
        ret.put(key, value);
    }

    this.keys.intersect(map2.keys).forEach { key ->
        val v1 = this.get(key);
        var v2 = map2.get(key);

        if (v1 == null) {
            ret.put(key, v2)
            return@forEach
        } else if (v2 == null) {
            ret.put(key, v1)
            return@forEach
        }

        var v1_type = v1::class.java
        var v2_type = v2::class.java

        if (v1_type.IsSimpleType() || v2_type.IsSimpleType()) {
            ret.put(key, v2)
            return@forEach
        }

        var v1_map = v1.ConvertType(Map::class.java) as Map<String, Any?>
        var v2_map = v2.ConvertType(Map::class.java) as Map<String, Any?>

        ret.put(key, v1_map.deepJoin(v2_map))
    }

    return ret;
}
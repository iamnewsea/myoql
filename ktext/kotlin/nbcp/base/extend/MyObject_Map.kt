package nbcp.base.extend


//顺序会变
fun <V> LinkedHashMap<String, V>.RenameKey(oldKey: String, newKey: String) {
    var index = this.keys.indexOf(oldKey);
    if (index < 0) {
        throw RuntimeException("找不到Key")
    }

    var value = this.get(oldKey)!!;
    this.remove(oldKey);
    this.put(newKey,value);

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





/**
 * 通过 path 获取 value,每级返回的值必须是 Map<String,V> 否则返回 null
 * @param key:
 */
fun <V> Map<String, V>.getPathValue(vararg keys: String): Any? {
    if (keys.any() == false) return null;
    var key = keys.first();
    var v = this.get(key)
    if (v == null) return null;

    var left_keys = keys.Slice(1);
    if (left_keys.any() == false) return v;

    if (v is Map<*, *>) {
        return (v as Map<String, V>).getPathValue(*left_keys.toTypedArray())
    }
    return v.toString()
}

fun <V> Map<String, V>.getStringValue(vararg keys: String): String {
    var v = this.getPathValue(*keys)
    if (v == null) return "";
    var v_type = v::class.java;
    if (v_type.isArray) {
        return (v as Array<String>).joinToString(",")
    } else if (List::class.java.isAssignableFrom(v_type)) {
        return (v as List<String>).joinToString(",")
    }
    return v.toString()
}

fun <V> Map<String, V>.getIntValue(vararg keys: String): Int {
    var v = getPathValue(*keys)
    if (v == null) return 0;
    return v.AsInt()
}


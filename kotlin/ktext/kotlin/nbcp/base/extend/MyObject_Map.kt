package nbcp.base.extend



fun <V> LinkedHashMap<String, V>.RenameKey(oldKey: String, newKey: String) {
    var index = this.keys.indexOf(oldKey);
    if (index < 0) {
        throw Exception("找不到Key")
    }

    var other = LinkedHashMap<String, V>();

    for (i in this.keys.indices) {
        var k = this.keys.elementAt(i);
        if (k == oldKey) {
            var value = this[oldKey];
            for (j in i + 1..this.size - 1) {
                k = this.keys.elementAt(j);
                other.put(k, this[k]!!);
            }

            for (j in i..this.size - 1) {
                k = this.keys.elementAt(i + 1);
                this.remove(k)
            }

            this.put(newKey, value!!);

            for (j in other.keys.indices) {
                k = other.keys.elementAt(j);
                this.put(k, other[k]!!);
            }
            break;
        }
    }
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


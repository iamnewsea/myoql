package nbcp.scope

import nbcp.comm.GetEnumStringField
import java.util.*


class ScopeStack : Stack<IScopeData>() {

    /**
     * 按类型获取当前域 ,  互斥枚举类型：枚举有 mutexGroup:String 属性。
     */

    inline fun <reified R : IScopeData> getScopeTypes(): Set<R> {
        return getScopeTypes(R::class.java);
    }

    fun <R : IScopeData> getScopeTypes(retType: Class<R>): Set<R> {
        if (this.size == 0) return setOf()

        var list = mutableSetOf<R>()
        for (i in this.indices.reversed()) {
            var item = this[i];
            if (retType.isAssignableFrom(item.javaClass)) {
                list.add(item as R);
            }
        }


        if (retType.isEnum) {
            var mutexGroupField = retType.GetEnumStringField()
            if (mutexGroupField != null && mutexGroupField.name == "mutexGroup") {
                var groups = mutableSetOf<String>()
                var removeItems = mutableSetOf<R>()
                for (i in list.indices) {
                    var item = list.elementAt(i);
                    var group = mutexGroupField.get(item).toString();
                    if (groups.contains(group)) {
                        removeItems.add(item);
                    } else {
                        groups.add(group)
                    }
                }

                list.removeAll(removeItems);
            }
        }
        return list;
    }

    /**
     * 获取指定 key 为 String 的 value
     * usingScope(StringScopeData("key","value")){
     *   scopes.getLatestStringScope("key")
     * }
     */
    fun getLatestStringScope(key: String): String {
        if (this.size == 0) return ""
        if (key.isEmpty()) return ""

        for (i in this.indices.reversed()) {
            var item = this[i];
            if (item is StringScopeData) {
                if (item.key == key) {
                    return item.value;
                } else {
                    continue;
                }
            }
        }

        return "";
    }

    /**
     * 查找最近添加的。
     * @param enumValues: 如果有值，则精确查找该值进行返回。
     */
    inline fun <reified R : IScopeData> GetLatest(vararg enumValues: R): R? {
        return GetLatest(R::class.java);
    }

    fun <R : IScopeData> GetLatest(clazz: Class<R>, vararg enumValues: R): R? {
        if (this.size == 0) return null

        for (i in this.indices.reversed()) {
            var item = this[i];
            if (clazz.isAssignableFrom(item.javaClass)) {
                if (enumValues.isEmpty() || enumValues.contains(item)) {
                    return item as R;
                } else {
                    continue;
                }
            }
        }
        return null;
    }
}

package nbcp.base.scope


import nbcp.base.extend.GetEnumStringField
import java.util.*
import kotlin.reflect.KClass


class ScopeStack : Stack<IScopeData>() {

    /**
     * 按类型获取当前域 ,  互斥枚举类型：枚举有 mutexGroup:String 属性。
     */
    inline fun <reified R : IScopeData> getScopeTypes(): Set<R> {
        return getScopeTypes(R::class.java);
    }

    fun <R : IScopeData, Any> getScopeTypes(retType: KClass<R>): Set<R> {
        return getScopeTypes(retType.java)
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
    inline fun <reified R : IScopeData> getLatest(vararg enumValues: R): R? {
        return getLatest(R::class.java, *enumValues);
    }

    fun <R : IScopeData> getLatest(type: KClass<R>, vararg enumValues: R): R? {
        return getLatest(type.java, *enumValues)
    }

    /**
     * @exception: 不能做日志,因为在 MyLogBackFilter 中会调用它!
     */
    fun <R : IScopeData> getLatest(type: Class<R>, vararg enumValues: R): R? {
        if (this.size == 0) return null

        for (i in this.indices.reversed()) {
            val item = this[i];
            if (type.isAssignableFrom(item.javaClass)) {
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

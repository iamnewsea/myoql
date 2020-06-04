package nbcp.comm

import org.slf4j.LoggerFactory
import nbcp.comm.*
import java.io.Serializable

/**
 * 尽量使用 JsonMap
 *
 * kotlin 的 linkedMapOf 本身是可以直接按值比较的。它的 hashCode() 函数返回值 依赖下面的内容
 * 1. 泛型参数类型
 * 2. keys 排序后的内容 hasCode()
 * 3. 按 keys 排序后的 values  hasCode()
 * ==>
 * linkMapOf<String,String>("name" to "udi","age","99") == linkMapOf<String,Any>("age" to "99","name" to "udi")
 * linkMapOf<String,String>("age","99") == linkMapOf<String,Any>("age" to "99")
 * linkMapOf<String,String>("age","99") != linkMapOf<String,Any>("age" to 99)
 */
open class StringKeyMap<T> : LinkedHashMap<String, T>, Serializable {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    constructor() : super() {
    }

    constructor(data: Map<String, T>) : this() {
        data.forEach {
            this.put(it.key, it.value);
        }
    }

    constructor(vararg pairs: Pair<String, T>) : this(pairs.toList()) {
    }

    constructor(pairs: List<Pair<String, T>>) : this() {
        for (p in pairs) {
            this.put(p.first, p.second);
        }
    }

    /**
     * 忽略大小写获取键值
     */
    fun getKeyIgnoreCase(key: String): T? {
        var key2 = this.keys.firstOrNull { it.VbSame(key) };
        if (key2 == null) {
            return null
        }
        return this[key2];
    }

//    operator fun plus(other: StringTypedMap<T>): StringTypedMap<T> {
//        var ret = StringTypedMap<T>()
//        ret.putAll(this)
//        ret.putAll(other)
//        return ret;
//    }


//    private fun getIndexs(key: String): Array<Int> {
//        if (key.length < 0) {
//            return arrayOf()
//        }
//        var index = key.indexOf('[');
//        if (index < 0) {
//            return arrayOf()
//        }
//        if (key.last() != ']') {
//            return arrayOf()
//        }
//        var source = key.Slice(index + 1, -1);
//        var endIndex = source.indexOf("...", 0);
//        if (endIndex >= 0) {
//            var list = mutableListOf<Int>()
//            var item = source.Slice(0, endIndex).AsInt(0);
//            var num4 = source.Slice((endIndex + 3)).AsInt(0);
//            while (item <= num4) {
//                list.add(item);
//                item++;
//            }
//            return list.toTypedArray()
//        }
//        return source.split(',').map { it.AsInt() }.distinct().toTypedArray()
//    }

    private fun toJsonValueString(value: Any?): String {
        if (value == null) return "null"
        var type = value::class.java
        if (type.IsSimpleType()) {
            if (type.IsNumberType()) {
                return "${value}"
            }
            if (type.IsBooleanType()) {
                return "${value}"
            } else {
                return """"${value}""""
            }
        } else if (Map::class.java.isAssignableFrom(type)) {
            return (value as StringKeyMap<*>).toJsonString()
        } else if (type.isArray) {
            return "[" + (value as Array<*>).map { toJsonValueString(it) }.joinToString(",") + "]"
        } else if (Collection::class.java.isAssignableFrom(type)) {
            return "[" + (value as Collection<*>).map { toJsonValueString(it) }.joinToString(",") + "]"
        } else {
            logger.warn(">>> StringTypedMap<T>.toJsonString 过程中遇到实体: ${type.name}")
            //遇到实体.
            return value.ToJson()
        }
    }

    fun toJsonString(): String {
        return "{" + this.filter {
            if (it.value == null) return@filter false
            if (!(it.value is String)) return@filter true
            if (it.value is String && (it.value as String).isEmpty()) {
                return@filter false
            }
            return@filter true
        }.map { """"${it.key}":${toJsonValueString(it.value)}""" }.joinToString(",") + "}"
    }
}

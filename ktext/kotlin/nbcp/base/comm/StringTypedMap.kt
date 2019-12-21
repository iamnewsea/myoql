package nbcp.comm

import org.slf4j.LoggerFactory
import nbcp.base.extend.*
import java.util.concurrent.ConcurrentHashMap

open class StringTypedMap<T> : LinkedHashMap<String, T> {
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

//    operator fun plus(other: StringTypedMap<T>): StringTypedMap<T> {
//        var ret = StringTypedMap<T>()
//        ret.putAll(this)
//        ret.putAll(other)
//        return ret;
//    }


    fun onlyHoldKeys(keys: Set<String>) {
        this.keys.minus(keys.toTypedArray()).forEach {
            this.remove(it)
        }
    }

    fun findByKey(KeyPath: String): Array<StringTypedMap<T>> {
        if (KeyPath.isEmpty()) {
            return arrayOf();
        }
        var path = KeyPath.split('/');
        if (path.size == 0) {
            return arrayOf();
        }
        var source = mutableListOf<StringTypedMap<T>>()


        var key = path.first();
        var indexs = this.getIndexs(key);
        var index = key.indexOf('[');
        if (index >= 0) {
            key = key.Slice(0, index);
        }

        var item = this[key] as StringTypedMap<T>;
        if (item != null) {
            source.add(item);
        } else {
            var objArray = this[key] as Array<*>
            if (objArray != null) {
                for (num3 in 0..(objArray.size - 1)) {
                    if ((indexs.size <= 0) || indexs.contains(num3)) {
                        var obj2 = objArray[num3];
                        var data2 = obj2 as StringTypedMap<T>;
                        if (data2 != null) {
                            source.add(data2);
                        }
                    }
                }
            }
        }

        if (path.size == 1) {
            return source.toTypedArray()
        }

        var ret = mutableListOf<StringTypedMap<T>>();
        source.forEach { o ->
            ret.addAll(o.findByKey(path.Skip(1).joinToString("/")));
        }
        return ret.toTypedArray()
    }


    private fun getIndexs(key: String): Array<Int> {
        if (key.length < 0) {
            return arrayOf()
        }
        var index = key.indexOf('[');
        if (index < 0) {
            return arrayOf()
        }
        if (key.last() != ']') {
            return arrayOf()
        }
        var source = key.Slice(index + 1, -1);
        var endIndex = source.indexOf("...", 0);
        if (endIndex >= 0) {
            var list = mutableListOf<Int>()
            var item = source.Slice(0, endIndex).AsInt(0);
            var num4 = source.Slice((endIndex + 3)).AsInt(0);
            while (item <= num4) {
                list.add(item);
                item++;
            }
            return list.toTypedArray()
        }
        return source.split(',').map { it.AsInt() }.distinct().toTypedArray()
    }

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
            return (value as StringTypedMap<*>).toJsonString()
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

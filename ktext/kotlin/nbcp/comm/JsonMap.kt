package nbcp.comm

import org.slf4j.LoggerFactory
import nbcp.utils.*
import java.lang.RuntimeException

open class JsonMap : StringKeyMap<Any?> {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
        //分派口.
//        private fun value2Serializable(value: Any?): Any? {
//            if (value == null) return null
//            var valueType = value::class.java
//
//            if (valueType.IsSimpleType()) {
//                return value
//            } else if (value is Map<*, *>) {
//                var json = JsonMap()
//                value.forEach {
//                    value2Serializable(it.value)?.also { value -> json.set(it.key.AsString(), value) }
//                }
//                return json
//            } else if (valueType.isArray) {
//                return (value as Array<*>).map { value2Serializable(it) }
//            } else if (Collection::class.java.isAssignableFrom(valueType)) {
//                return (value as Collection<*>).map { value2Serializable(it) }
//            } else {
//                var ret = JsonMap()
//                var clazz = valueType;
//                clazz.AllFields.forEach { entityField ->
//                    var entityFieldName = entityField.name;
//                    entityField.isAccessible = true;
//                    var entityFieldValue = entityField.get(value)
//                    value2Serializable(entityFieldValue)?.also { ret.set(entityFieldName, it) }
//                }
//
//                return ret
//            }
//        }
//
//        fun <T : Any> loadFromEntity(entity: T): JsonMap {
////            if (entity == null) return JsonMap();
//
//            var ret = value2Serializable(entity)
//            if (ret is JsonMap) {
//                return ret
//            }
//            throw RuntimeException("实体${entity::class.java}转JsonMap出错!")
//        }

        private fun set_key_value(ret: JsonMap, keys: List<String>, value: String) {
            if (keys.any() == false) return;

            var key = keys.first();
            if (keys.size == 1) {
                //如果指明是数组 []
//                var isArray = false;
                if (key.endsWith("[]")) {
                    key = key.Slice(0, -2);
//                    isArray = true;

                    if (ret.containsKey(key) == false) {
                        ret.put(key, mutableListOf<String>())
                    }
                }

                if (ret.containsKey(key) == false) {
                    ret.put(key, value)
                    return;
                }

                var v_list = mutableListOf<String>()
                var dbValue = ret[key];
                if (dbValue is MutableList<*>) {
                    v_list = dbValue as MutableList<String>;
                } else {
                    if (dbValue != null) {
                        v_list.add(dbValue.AsString())
                    }
                }

                v_list.add(value)
                ret.set(key, v_list);
                return;
            }

            if (ret.containsKey(key) == false) {
                ret.put(key, JsonMap());
            }

            var subObj = ret.get(key)!!
            if (subObj is JsonMap == false) {
                throw RuntimeException("${keys.joinToString(".")},已有类型:${subObj::class.java.name}")
            }

            set_key_value(subObj, keys.Slice(1), value);
        }

        @JvmStatic
        fun loadFromUrl(urlQueryString: String): JsonMap {
            val ret = JsonMap()
            var urlQuery = urlQueryString.trim()
            if (urlQuery.isEmpty()) return ret;

            val list = urlQuery.split("&").dropLastWhile { it.isEmpty() }.toTypedArray()
            for (item in list) {
                val kv = item.split("=").dropLastWhile { it.isEmpty() }.toTypedArray()
                if (kv.size < 1) {
                    continue;
                }

                var key = kv[0];
                var value = if (kv.size == 1) null else JsUtil.decodeURIComponent(kv[1]);
                if (value == null) {
                    continue;
                }
                //如果 key 是多级对象。
                var key_parts = key.split(".");
                set_key_value(ret, key_parts, value);
            }

            return ret
        }
    }

    constructor() : super() {
    }

    constructor(data: Map<String, Any?>) : super(data) {
    }

    constructor(vararg pairs: Pair<String, Any?>) : super(*pairs) {
    }

    constructor(data: Collection<Pair<String, Any?>>) : super(data) {
    }

    operator fun plus(other: JsonMap): JsonMap {
        var ret = JsonMap()
        ret.putAll(this)
        ret.putAll(other)
        return ret;
    }


    operator fun plusAssign(other: JsonMap) {
        this.putAll(other)
    }
}


//差异数据: T 表示第一个数据，R表示第2个数据。如： 123， 234 ，common = 23 , more = 1 , less = 4
data class DiffData<T, R> @JvmOverloads constructor(
        //公共索引,key=第一个数据索引， value = 第二个数据索引。
        var commonIndexMap: Map<Int, Int> = mapOf<Int, Int>(),
        //第一部分多出的数据
        var more1: List<T> = listOf<T>(),
        //第一部分公共数据
        var common1: List<T> = listOf<T>(),
        //第二部分公共数据
        var common2: List<R> = listOf<R>(),
        //第二部分多出的数据。
        var more2: List<R> = listOf<R>()
) {
    companion object {
        //把数据分隔为 DiffData，
        @JvmStatic
        inline fun <T, R> load(data: Iterable<T>, other: Collection<R>, equalFunc: (T, R) -> Boolean): DiffData<T, R> {
            var diff = DiffData<T, R>();

            var indexList = data.IntersectIndeies(other, equalFunc);

            var keyIndexList = indexList.keys;
            var valueIndexList = indexList.values;

            diff.commonIndexMap = indexList;
            diff.more1 = data.filterIndexed { index, _ -> keyIndexList.contains(index) == false };
            diff.common1 = data.filterIndexed { index, _ -> keyIndexList.contains(index) };
            diff.common2 = other.filterIndexed { index, _ -> valueIndexList.contains(index) };
            diff.more2 = other.filterIndexed { index, _ -> valueIndexList.contains(index) == false };
            return diff;
        }
    }

    fun isSame(): Boolean {
        if (this.more1.any()) return false;
        if (this.more2.any()) return false;

        return this.common1.size == this.common2.size;
    }
}
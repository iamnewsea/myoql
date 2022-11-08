package nbcp.myoql.db.mongo

import nbcp.base.comm.*
import nbcp.base.extend.IsCollectionType
import nbcp.base.extend.IsMapType
import nbcp.base.extend.IsSimpleType
import org.apache.commons.collections4.map.LinkedMap
import java.util.*

class MongoWhereClip() : LinkedList<JsonMap>() {

    constructor(map: Map<String, Any?>) : this() {
        if (map is JsonMap) {
            this.add(map);
            return;
        }
        this.add(JsonMap(map))
    }

    fun putAll(map: Map<String, Any?>) {
        if (map is JsonMap) {
            this.add(map);
            return;
        }

        this.add(JsonMap(map));
    }

    fun addWhere(key: String, value: Any?) {
        this.add(JsonMap(key to value))
    }

    /**
     * 从根级查找指定条件的值。
     * @param column , 可能是  _id , user._id
     * @return 可能是简单类型，也可能是List类型
     */
    fun findValueFromRootLevel(column: String): Any? {
        for (map in this) {
            val findedValue = getValueFromMap(map, column);
            if (findedValue != null) {
                return findedValue;
            }
        }
        return null;
    }

    private fun getValueFromMap(map: Map<String, Any?>, column: String): Any? {
        if (map.keys.size == 1 && map.keys.first() == "\$and") {
            var vs = map.values.first() as Collection<Map<String, Any?>>?

            if (vs == null || !vs.any()) {
                return null;
            }

            vs.stream()
                .map { getValueFromMap(it, column) }
                .filter { it != null }
                .findFirst()
                .apply {
                    if (this.isPresent) {
                        return this.get()
                    }
                    return null;
                }
        }

        var value = map.get(column);
        if (value == null) return null;

        var value_type = value::class.java;
        if (value_type.IsSimpleType()) return value;

        if (value_type.IsMapType) {
            var map = value as Map<String, Any?>
            var in_value = map.get("\$in")
            if (in_value != null) {
                var in_value_type = in_value::class.java;
                if (in_value_type.IsCollectionType) {
                    return in_value;
                }
            }
        }
        return value;
    }
}
package nbcp.db.mongo

import nbcp.comm.*
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

    /**
     * 从根级查找指定条件的值。
     * @param column , 可能是  _id , user._id
     * @return 可能是简单类型，也可能是List类型
     */
    fun findValueFromRootLevel(column: String): Any? {
        return this.firstOrNull { map ->
            return@firstOrNull getValueFromMap(map, column) != null
        }
    }

    private fun getValueFromMap(map: JsonMap, column: String): Any? {
        var value = map.get(column);
        if (value == null) return null;

        var value_type = value::class.java;
        if (value_type.IsSimpleType()) return value_type;

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
        return null;
    }
}
@file:JvmName("MyOqlMongo")
@file:JvmMultifileClass

package nbcp.db.mongo

import nbcp.comm.AsInt
import nbcp.comm.AsString
import nbcp.comm.Slice
import nbcp.utils.*
import org.bson.Document
import org.bson.types.ObjectId


/**
 * 处理Object类型的数据为 {$oid}
 */
fun Map<*, *>.procWithMongoScript(): Map<*, *> {

    /**
     * 处理 value 是 Object 的情况。
     */
    fun procObjectId(value: Any?): Any? {
        if (value == null) return null;

        if (value is String && ObjectId.isValid(value)) {
            return value.toOIdJson();
        } else if (value is ObjectId) {
            return value.toString().toOIdJson()
        }
        return null;
    }


    RecursionUtil.recursionJson(this,  { json ->
        var doc = json as MutableMap<String, Any>;
        doc.keys.forEach { key ->
            /**情况：
             * 1. { id : 值 }
             * 2. { id : { $操作符 : 值 } }   $ne
             *3. { id:  { $操作符:  [值] } }    $in
             */
            var value = doc.get(key);

            if (value == null) {
                return@forEach;
            }

            if (key == "_id" || key.endsWith("._id")) {
                var value_oid = procObjectId(value);
                if (value_oid != null) {
                    doc.set(key, value_oid);
                } else if (value is MutableMap<*, *>) {
                    var value_map = value as MutableMap<String, Any>

                    value_map.keys.forEach forEach2@{ op ->
                        if (op == "\$oid") {
                            return@forEach2
                        }

                        var value2 = value_map.get(op);
                        if (value2 == null) {
                            return@forEach2
                        }

                        var value2_oid = procObjectId(value2);

                        if (value2_oid != null) {
                            value_map.set(op, value2_oid);
                        } else if (value2 is Collection<*>) {
                            value_map.set(op, value2.map { procObjectId(it) ?: it });
                        } else if (value2 is Array<*>) {
                            value_map.set(op, value2.map { procObjectId(it) ?: it });
                        }
                    }
                }
            }
        }

        return@recursionJson true;
    })

    return this;
}

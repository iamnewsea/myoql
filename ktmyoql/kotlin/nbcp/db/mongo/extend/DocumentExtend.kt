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
 * 根据路径查找值.
 */
fun Document.GetComplexPropertyValue(property: String): String {
    var sects = property.split(".");

    if (sects.size == 0) return "";


    return this.GetComplexPropertyValue(*sects.toTypedArray()).AsString()
}

/**
 * 根据路径查找值.
 * @param eachProperty : 每个key,每次只取第一个，递归时传递剩余的keys.
 */
fun Document.GetComplexPropertyValue(vararg eachProperty: String): Any? {
    if (eachProperty.size == 0) return null;

    var key = eachProperty[0];

//    if (key == "id") {
//        key = "_id";
//    }

    var retVal: Any? = null;
    var aryIndex = key.indexOf('[');

    if (aryIndex > 0) {
        var aryPropertys = key.substring(aryIndex).Slice(1, -1).split("][").map { it.AsInt() };
        key = key.Slice(0, aryIndex);

        var aryValue = (this.get(key) as ArrayList<*>);

        aryPropertys.take(aryPropertys.size - 1).forEach {
            aryValue = aryValue[it] as ArrayList<*>;
        }

        retVal = aryValue[aryPropertys.last()]
    } else {
        retVal = this.get(key);


        /**
         * 如果取不到值。则尝试 id , _id
         */
        if (eachProperty.size == 1 && retVal == null) {
            if (key == "id") {
                retVal = this.get("_id");
            } else if (key == "_id") {
                retVal = this.get("id")
            }
        }
    }

    if (eachProperty.size == 1) {
        return retVal;
    }

    return (retVal as Document).GetComplexPropertyValue(*eachProperty.takeLast(eachProperty.size - 1).toTypedArray());
}

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


    RecursionUtil.recursionJson(this,"", { json,pKey ->
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

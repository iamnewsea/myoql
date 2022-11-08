@file:JvmName("MyOqlMongo")
@file:JvmMultifileClass

package nbcp.myoql.db.mongo.extend

import nbcp.base.comm.*
import nbcp.base.extend.ToJson
import nbcp.base.utils.*
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.query.Criteria
import java.lang.RuntimeException

/**
 * 把 where 转换为表达式，会有feild的基础上额外添加一个 $ .如：
 * "field" : { "$gt" : 1}
 * -->
 * "$gt" : ["$field" , 1 ]
 */
fun Criteria.toExpression(): JsonMap {
    var ret = JsonMap()
    this.criteriaObject.forEach { ent ->
        var key = "$" + ent.key
        var value = ent.value

        if (value is Map<*, *>) {
            if (value.size == 1) {
                var first = value.entries.first()
                ret.set(first.key.toString(), arrayOf(key, first.value))
            } else {
                throw RuntimeException("不识别的表达式: " + value.ToJson())
            }
        }
        //简单类型
        else {
            ret.set("\$eq", arrayOf(key, value))
        }
    }
    return ret
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

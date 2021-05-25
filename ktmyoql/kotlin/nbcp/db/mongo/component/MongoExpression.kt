package nbcp.db.mongo


import nbcp.comm.ToJson
import nbcp.comm.JsonMap
import nbcp.comm.JsonSceneEnumScope
import org.springframework.data.mongodb.core.query.Criteria
import java.lang.RuntimeException

/**
 * 把 where 转换为表达式，如：
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
                throw RuntimeException("不识别的表达式: " + value.ToJson(JsonSceneEnumScope.Db))
            }
        }
        //简单类型
        else {
            ret.set("\$eq", arrayOf(key, value))
        }
    }
    return ret
}

/** 从里往外写。
 * 例：
 * totalSaleAmount: { $sum: { $multiply: [ "$price", "$quantity" ] } }
 * avgAmount: { $avg: "$amount" }
 * --->
 * db.mongo
 *  .op(PipeLineOperatorEnum.multiply, arrayOf("$price","$quantity"))
 *  .accumulate(PipeLineAccumulatorOperatorEnum.sum)
 *  .As("totalSaleAmount")
 *
 * db.mongo
 *  .op("$amount")
 *  .accumulate(PipeLineOperatorEnum.avg)
 *  .As("totalSaleAmount")
 *
 *  运算符：
 *  ( "abc" mongo_multi "def" ) mongo
 */

class MongoExpression : JsonMap {
    constructor() : super() {
    }

    constructor(vararg pairs: Pair<String, Any?>) : super(pairs.toList()) {
    }

    /**
     * 聚合
     */
    fun accumulate(operator: PipeLineAccumulatorOperatorEnum): MongoExpression {
        return MongoExpression("$" + operator.toString() to this)
    }


    /**
     * 返回 列名:表达式
     */
    fun As(columnName: String): JsonMap {
        return JsonMap(columnName to this)
    }
}

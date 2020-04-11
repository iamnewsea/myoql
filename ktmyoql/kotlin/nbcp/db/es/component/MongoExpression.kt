//package nbcp.db.es
//
//
//import nbcp.base.extend.ToJson
//import nbcp.comm.JsonMap
//import java.lang.RuntimeException
//
///**
// * 把 where 转换为表达式，如：
// * "field" : { "$gt" : 1}
// * -->
// * "$gt" : ["$field" , 1 ]
// */
//fun Criteria.toExpression(): JsonMap {
//    var ret = JsonMap()
//    this.criteriaObject.forEach { ent ->
//        var key = "$" + ent.key
//        var value = ent.value
//
//        if (value is Map<*, *>) {
//            if (value.size == 1) {
//                var first = value.entries.first()
//                ret.set(first.key.toString(), arrayOf(key, first.value))
//            } else {
//                throw RuntimeException("不识别的表达式: " + value.ToJson())
//            }
//        }
//        //简单类型
//        else {
//            ret.set("\$eq", arrayOf(key, value))
//        }
//    }
//    return ret
//}
//
///** 从里往外写。
// * 例：
// * totalSaleAmount: { $sum: { $multiply: [ "$price", "$quantity" ] } }
// * avgAmount: { $avg: "$amount" }
// * --->
// * db.es
// *  .op(PipeLineOperatorEnum.multiply, arrayOf("$price","$quantity"))
// *  .accumulate(PipeLineAccumulatorOperatorEnum.sum)
// *  .As("totalSaleAmount")
// *
// * db.es
// *  .op("$amount")
// *  .accumulate(PipeLineOperatorEnum.avg)
// *  .As("totalSaleAmount")
// *
// *  运算符：
// *  ( "abc" es_multi "def" ) es
// */
//
//class EsExpression : JsonMap {
//    constructor() : super() {
//    }
//
//    constructor(vararg pairs: Pair<String, Any?>) : super(pairs.toList()) {
//    }
//
//    /**
//     * 聚合
//     */
//    fun accumulate(operator: PipeLineAccumulatorOperatorEnum): EsExpression {
//        return EsExpression("$" + operator.toString() to this)
//    }
//
//
//    /**
//     * 返回 列名:表达式
//     */
//    fun As(columnName: String): JsonMap {
//        return JsonMap(columnName to this)
//    }
//}

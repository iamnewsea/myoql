package nbcp.db.mongo


import nbcp.comm.ToJson
import nbcp.comm.JsonMap
import nbcp.scope.*
import org.springframework.data.mongodb.core.query.Criteria
import java.lang.RuntimeException



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
    fun As(columnName: String): MongoExpression {
        return MongoExpression(columnName to this)
    }

    fun toMongoColumnName(): MongoColumnName {
        return MongoColumnName(this.ToJson())
    }
}

package nbcp.myoql.db.mongo


import nbcp.myoql.db.mongo.enums.PipeLineAccumulatorOperatorEnum
import nbcp.base.comm.*;
import nbcp.base.db.*;
import nbcp.base.enums.*;
import nbcp.base.extend.*;
import nbcp.base.utils.*;
import nbcp.myoql.db.enums.*
import nbcp.myoql.db.*;
import nbcp.myoql.db.comm.*
import nbcp.myoql.db.mongo.base.MongoColumnName


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

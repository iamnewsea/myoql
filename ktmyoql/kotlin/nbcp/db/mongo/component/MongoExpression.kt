package nbcp.db.mongo

import nbcp.base.extend.MyRawString
import nbcp.base.extend.ToJson
import org.springframework.data.mongodb.core.query.Criteria


fun Criteria.toExpression(): MongoExpression {
    return MongoExpression(this.criteriaObject.toJson(), true)
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

class MongoExpression(value: String = "", val isObjectValue: Boolean = false) : MyRawString(value) {
    override fun toString(): String {
        var ret = super.toString();

        if (isObjectValue) {
            return "{" + ret + "}"
        }
        return ret
    }


    /**
     * 聚合
     */
    fun accumulate(operator: PipeLineAccumulatorOperatorEnum): MongoExpression {
        return MongoExpression("""$${operator}:"${this.toString()}""", true)
    }


    /**
     * 返回 列名:表达式
     */
    fun As(columnName: String): MyRawString {
        return MyRawString(""""${columnName}":${this.toString()}""")
    }
}

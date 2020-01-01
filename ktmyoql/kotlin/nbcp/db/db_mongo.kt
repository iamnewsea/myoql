package nbcp.db

import nbcp.base.extend.MyRawString
import nbcp.base.extend.ToJson
import nbcp.comm.JsonMap
import nbcp.db.mongo.*
import org.springframework.data.mongodb.core.query.Criteria

object db_mongo {

    //----------------mongo expression-------------

//    fun times(rawValue: String):PipeLineExpression{
//        return db.mongo.op(PipeLineOperatorEnum.multiply , arrayOf())
//    }

    fun cond(ifExpression: Criteria, trueExpression: String, falseExpression: String): MongoExpression {
        return op(PipeLineOperatorEnum.cond, arrayOf(MyRawString(ifExpression.criteriaObject.toJson()), trueExpression, falseExpression))
    }

    fun cond(ifExpression: Criteria, trueExpression: MongoExpression, falseExpression: MongoExpression): MongoExpression {
        return op(PipeLineOperatorEnum.cond, arrayOf(MyRawString(ifExpression.criteriaObject.toJson()), trueExpression, falseExpression))
    }

    fun cond(ifExpression: Criteria, trueExpression: String, falseExpression: MongoExpression): MongoExpression {
        return op(PipeLineOperatorEnum.cond, arrayOf(MyRawString(ifExpression.criteriaObject.toJson()), trueExpression, falseExpression))
    }

    fun cond(ifExpression: Criteria, trueExpression: MongoExpression, falseExpression: String): MongoExpression {
        return op(PipeLineOperatorEnum.cond, arrayOf(MyRawString(ifExpression.criteriaObject.toJson()), trueExpression, falseExpression))
    }

    fun cond(ifExpression: MongoExpression, trueExpression: MongoExpression, falseExpression: MongoExpression): MongoExpression {
        return op(PipeLineOperatorEnum.cond, arrayOf(ifExpression, trueExpression, falseExpression))
    }


    fun op(rawValue: String): MongoExpression {
        return MongoExpression("\"" + rawValue + "\"")
    }

    fun value(rawValue: String): MongoExpression {
        return MongoExpression(rawValue)
    }

    fun op(operator: PipeLineOperatorEnum, rawValue: String): MongoExpression {
        return MongoExpression("""$${operator}:"${rawValue}"""", true)
    }

    fun op(operator: PipeLineOperatorEnum, rawValue: MongoExpression): MongoExpression {
        return MongoExpression("$${operator}:${rawValue.toString()}", true)
    }

    fun op(operator: PipeLineOperatorEnum, rawValue: Array<*>): MongoExpression {
        return MongoExpression("$${operator}:${rawValue.ToJson()}", true)
    }


    fun op(operator: PipeLineOperatorEnum, rawValue: JsonMap): MongoExpression {
        return MongoExpression("$${operator}:${rawValue.ToJson()}", true)
    }

}
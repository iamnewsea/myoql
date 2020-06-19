@file:JvmName("MyOqlMongo")
@file:JvmMultifileClass

package nbcp.db.mongo

import com.mongodb.BasicDBList
import org.bson.BasicBSONObject
import org.springframework.data.mongodb.core.query.Criteria

/**
 * Created by udi on 17-7-10.
 */
/**
 * 另一种形式的条件。值可以是字段。
 */
fun MongoColumnName.match_expr (op:String, to: MongoColumnName): Criteria {
    var d2 = BasicDBList();
    d2.add("$" + this.toString())
    d2.add("$" + to)

    var dict = BasicBSONObject()
    dict.put("$" + op, d2)

    return Criteria.where("$" + "expr").`is`(dict);
}

infix fun MongoColumnName.match_expr_equal(to: MongoColumnName): Criteria {
    return this.match_expr("eq",to);
}


infix fun MongoColumnName.match_expr_not_equal(to: MongoColumnName): Criteria {
    return this.match_expr("ne",to);
}

infix fun MongoColumnName.match_expr_gte(to: MongoColumnName): Criteria {
    return this.match_expr("gte",to);
}

infix fun MongoColumnName.match_expr_lte(to: MongoColumnName): Criteria {
    return this.match_expr("lte",to);
}


infix fun MongoColumnName.match_expr_greaterThan(to: MongoColumnName): Criteria {
    return this.match_expr("gt",to);
}

infix fun MongoColumnName.match_expr_lessThan(to: MongoColumnName): Criteria {
    return this.match_expr("lt",to);
}

infix fun MongoColumnName.match_expr_between(to: MongoColumnName): Criteria {
    return this.match_expr("between",to);
}
package nbcp.db.mongo

import com.mongodb.BasicDBList
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.client.model.Filters
import nbcp.comm.*
import org.bson.BSONObject
import org.bson.BasicBSONObject
import org.bson.BsonString
import org.bson.Document
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import nbcp.db.IdName
import nbcp.base.extend.*
import nbcp.db.mongo.*
import nbcp.db.mongo.component.MongoTypeEnum
import java.lang.reflect.ParameterizedType
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.regex.Pattern

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
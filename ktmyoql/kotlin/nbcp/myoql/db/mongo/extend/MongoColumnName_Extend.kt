package nbcp.myoql.db.mongo.extend

import nbcp.base.comm.JsonMap
import nbcp.myoql.db.mongo.base.MongoColumnName
import org.springframework.data.mongodb.core.query.Criteria

/**
 * Created by udi on 17-7-10.
 */

//private fun proc_mongo_key(key: MongoColumnName): String {
//    var key = key;
//    var keyString = key.toString();
//    if (keyString == "id") {
//        key = MongoColumnName("_id")
//    } else if (keyString.endsWith(".id")) {
//        key = MongoColumnName(keyString.slice(0..keyString.length - 4) + "._id")
//    }
//
//    return key.toString();
//}

class MongoColumnTranslateResult(
    var key: MongoColumnName,
    var value: Any?,
    var changed: Boolean = false
)

infix fun String.mongoEquals(to: Any?): Criteria {
    return MongoColumnName(this).mongoEquals(to)
}
///**
// * 用于 project
// */
//infix fun String.match_filter(value: Criteria): BasicDBObject {
//    var key = this;
//    return Criteria.where(key).`filter`(value);
//}


fun String.toOIdJson(): JsonMap {
//    if(ObjectId.isValid(this) == false)
    return JsonMap("\$oid" to this)
}
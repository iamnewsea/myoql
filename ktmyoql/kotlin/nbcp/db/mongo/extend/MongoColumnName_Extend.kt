@file:JvmName("MyOqlMongo")
@file:JvmMultifileClass

package nbcp.db.mongo

import nbcp.comm.*
import nbcp.db.db
import org.bson.BasicBSONObject
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.query.Criteria
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.regex.Pattern

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


infix fun MongoColumnName.match_size(value: Int): Criteria {
    return this.match_size(value);
}


/**
 * 模糊查询，用法：
 * mor.code.qrCodeInfo.product.name match_pattern "国际"
 * 即：内容包含 "国际"。
 *
 * mor.code.qrCodeInfo.product.name match_pattern "^国际"
 * 即：内容以 "国际" 开头。
 *
 * mor.code.qrCodeInfo.product.name match_pattern "国际$"
 * 即：内容以 "国际" 结尾。
 * @param pattern: 不会转义
 */
infix fun MongoColumnName.match_pattern(pattern: String): Criteria {
    return this.match_pattern(pattern)
}

/**
 * @param like: 会对查询内容中的特殊字符转义，避免与正则表达式冲突
 */
infix fun MongoColumnName.match_like(like: String): Criteria {
    return this.match_like(like);
}


infix fun MongoColumnName.match_not_equal(value: Any?): Criteria {
    return this.match_not_equal(value);
}


/* mongo 3.4
infix fun String.match_dbColumn_3_4(to: Any?): Criteria {
    var (key, to) = db.mongo.proc_mongo_key_value(this, to);

    return Criteria.where("$" + "where").`is`("this.${key} == this.${to}");// Pair<String, T>(this, to);
}
*/

infix fun String.match(to: Any?): Criteria {
    return MongoColumnName(this).match(to)
}

infix fun MongoColumnName.match(to: Any?): Criteria {
    return this.match(to);
}

//array_all
infix fun MongoColumnName.match_all(to: Array<*>): Criteria {
    return this.match_all(to);
}

//infix fun <T> String.match_like(to: T): Criteria {
//
//    return Criteria.where(this).`alike`(to);// Pair<String, T>(this, to);
//}


infix fun MongoColumnName.match_type(to: MongoTypeEnum): Criteria {
    return this.match_type(to);
}


infix fun MongoColumnName.match_gte(to: Any): Criteria {
    return this.match_gte(to);
}

infix fun MongoColumnName.match_lte(to: Any): Criteria {
    return this.match_lte(to);
}

infix fun MongoColumnName.match_greaterThan(to: Any): Criteria {
    return this.match_greaterThan(to);
}

infix fun MongoColumnName.match_lessThan(to: Any): Criteria {
    return this.match_lessThan(to);
}

/**
 * 大于等于并且小于。
 */
infix fun MongoColumnName.match_between(value: Pair<Any, Any>): Criteria {
    return this.match_between(value);
}

infix fun MongoColumnName.match_in(to: Collection<*>): Criteria {
    return this.match_in(to)
}

//db.test1.find({"age":{"$in":['值1','值2',.....]}})
infix fun MongoColumnName.match_in(to: Array<*>): Criteria {
    return this.match_in(to)
}

infix fun MongoColumnName.match_notin(to: Array<*>): Criteria {
    return this.match_notin(to)
}

infix fun MongoColumnName.match_notin(to: Collection<*>): Criteria {
    return this.match_notin(to)
}



/**
 * 用法：
 * 判断数组没有值，好处理： tags match_size 0
 * 判断数组有值,转化为：第一元素是否存在  MongoColumnName("tags.0")  match_exists true
 */
infix fun MongoColumnName.match_exists(value: Boolean): Criteria {
    return this.match_exists(value);
}

/**
 * 用于 数组的 elemMatch！
 * @sample
 * 如附件字段
 *  tags: [ {name:"a", score: 10},{name:"b", score: 50} ]
 *
 *  mongoshell 查询
 *  db.getCollection('sysAnnex').find(
 * {   tags:  { $elemMatch: { "score" : 5 } } },
 * {   tags:  { $elemMatch: { "score" : 5 } } }
 * )
 *
 * 程序：
 * mor.base.sysAnnex.query()
 *   .where_select_elemMatch { it.tags  match_elemMatch ( MongoColumName("score") match 5) }
 *   .toList()
 * ---
 * 如果附加字段是简单类型的数组，如:
 * tags: ["a","b"]
 *
 * mongoshell 查询：
 *  db.getCollection('sysAnnex').find(
 * {   tags:  { $elemMatch: { "$eq" : "a" } } },
 * {   tags:  { $elemMatch: { "$eq" : "a" } } }
 * )
 *
 * 程序：
 * mor.base.sysAnnex.query()
 *   .where_select_elemMatch { it.tags  ,  MongoColumName() match "a" }
 *   .toList()
 *
 * https://docs.mongodb.com/manual/reference/operator/query/elemMatch/index.html
 * https://docs.mongodb.com/manual/reference/operator/projection/elemMatch/index.html
 * @param value: 和普通的条件是不一样的。
 */
infix fun MongoColumnName.match_elemMatch(value: Map<String, Any?>): Criteria {
    return this.match_elemMatch(value);
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
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
    return Criteria.where(this.toString()).size(value);
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
    return Criteria.where(this.toString()).regex(Pattern.compile(pattern, Pattern.CASE_INSENSITIVE))
}

/**
 * @param like: 会对查询内容中的特殊字符转义，避免与正则表达式冲突
 */
infix fun MongoColumnName.match_like(like: String): Criteria {
    return this match_pattern "${getSafeRegText(like)}"
}

private fun getSafeRegText(value: String): String {
    //https://www.cnblogs.com/ysk123/p/9858387.html

    var v = value;

    """\/|()[]{}*+.?^${'$'}""".forEach {
        v = v.replace(it.toString(), "\\${it}")
    }
    return v;
}

infix fun MongoColumnName.match_not_equal(value: Any?): Criteria {
    val (key, toValue) = db.mongo.proc_mongo_key_value(this, value);
    return Criteria.where(key).`ne`(toValue)
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
    val (key, toValue) = db.mongo.proc_mongo_key_value(this, to);

    return Criteria.where(key).`is`(toValue);// Pair<String, T>(this, to);
}

//array_all
infix fun MongoColumnName.match_all(to: Array<*>): Criteria {
    val (key, tos) = db.mongo.proc_mongo_key_value(this, to.toSet())

    if (tos is Array<*>) {
        return Criteria.where(key).`all`(*(tos as Array<*>));
    }
    return Criteria.where(key).`all`(*(tos as Collection<*>).toTypedArray());
}

//infix fun <T> String.match_like(to: T): Criteria {
//
//    return Criteria.where(this).`alike`(to);// Pair<String, T>(this, to);
//}


infix fun MongoColumnName.match_type(to: MongoTypeEnum): Criteria {
    val (key, _) = db.mongo.proc_mongo_key_value(this, to);

    return Criteria.where(key).`type`(to.value);// Pair<String, T>(this, to);
}


infix fun MongoColumnName.match_gte(to: Any): Criteria {
    val (key, toValue) = db.mongo.proc_mongo_key_value(this, to);
    return Criteria.where(key).gte(toValue!!);
}

infix fun MongoColumnName.match_lte(to: Any): Criteria {
    val (key, toValue) = db.mongo.proc_mongo_key_value(this, to);
    return Criteria.where(key).lte(toValue!!);
}

infix fun MongoColumnName.match_greaterThan(to: Any): Criteria {
    val (key, toValue) = db.mongo.proc_mongo_key_value(this, to);
    return Criteria.where(key).gt(toValue!!);
}

infix fun MongoColumnName.match_lessThan(to: Any): Criteria {
    val (key, toValue) = db.mongo.proc_mongo_key_value(this, to);
    return Criteria.where(key).lt(toValue!!);
}

/**
 * 大于等于并且小于。
 */
infix fun MongoColumnName.match_between(value: Pair<Any, Any>): Criteria {
    var (key, value2) = db.mongo.proc_mongo_key_value(this, value);
    var pair = value2 as Pair<Any, Any>

    var dict = BasicBSONObject()
    dict.put("\$gte", pair.first)
    dict.put("\$lt", pair.second)
    return Criteria.where(key).`is`(dict)
    //return Criteria.where(key).gte(from).andOperator(Criteria.where(key).lt(to))
}

infix fun MongoColumnName.match_in(to: Collection<*>): Criteria {
    return this.match_in(to.toTypedArray())
}

//db.test1.find({"age":{"$in":['值1','值2',.....]}})
infix fun MongoColumnName.match_in(to: Array<*>): Criteria {
    var (key, tos) = db.mongo.proc_mongo_key_value(this, to.toSet())
    if (tos is Array<*>) {
        return Criteria.where(key).`in`(*(tos as Array<*>));
    }
    return Criteria.where(key).`in`(*(tos as Collection<*>).toTypedArray());
}

infix fun MongoColumnName.match_notin(to: Array<*>): Criteria {
    var (key, tos) = db.mongo.proc_mongo_key_value(this, to.toSet())
    if (tos is Array<*>) {
        return Criteria.where(key).`nin`(*(tos as Array<*>));
    }
    return Criteria.where(key).`nin`(*(tos as Collection<*>).toTypedArray());
}

infix fun MongoColumnName.match_notin(to: Collection<*>): Criteria {
    return this.match_notin(to.toTypedArray())
}

/**
 * 用法：
 * 判断数组没有值，好处理： tags match_size 0
 * 判断数组有值,转化为：第一元素是否存在  MongoColumnName("tags.0")  match_exists true
 */
infix fun MongoColumnName.match_exists(value: Boolean): Criteria {
    var (key) = db.mongo.proc_mongo_key_value(this, null);
    return Criteria.where(key).`exists`(value);
}

/**
 * field match_hasValue true  => field exists  and field != null
 * field match_hasValue false  => field not exists  or field == null
 */
infix fun MongoColumnName.match_hasValue(value: Boolean): Criteria {
    if (value) {
        return this.match_exists(true).match_and(this.match_not_equal(null));
    }

    return this.match_exists(false).match_or(this.match(null));
}


/**
 * 用于 数组的 match
 * https://docs.mongodb.com/manual/reference/operator/query/elemMatch/index.html
 * https://docs.mongodb.com/manual/reference/operator/projection/elemMatch/index.html
 */
infix fun MongoColumnName.match_elemMatch(value: Criteria): Criteria {
    var (key) = db.mongo.proc_mongo_key_value(this, null);
    return Criteria.where(key).`elemMatch`(value);
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
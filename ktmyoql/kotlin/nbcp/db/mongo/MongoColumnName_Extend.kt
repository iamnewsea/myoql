package nbcp.db.mongo

import com.mongodb.BasicDBList
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.client.model.Filters
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

private fun proc_mongo_key(key: MongoColumnName): String {
    var key = key;
    if (key.toString() == "id") {
        key = MongoColumnName("_id")
    } else if (key.endsWith(".id")) {
        key = key.slice(0..key.length - 4) + "._id";
    }

    return key.toString();
}

private fun proc_mongo_match(key: MongoColumnName, value: Any?): Pair<String, Any?> {
    var key = key
    var isId = false;
    if (key.toString() == "id") {
        key = MongoColumnName("_id")
        isId = true;
    } else if (key.toString() == "_id") {
        isId = true;
    } else if (key.endsWith(".id")) {
        key = key.slice(0..key.length - 4) + "._id";
        isId = true;
    } else if (key.endsWith("._id")) {
        isId = true;
    }

    var value = value;
    if (value != null) {
        var type = value::class.java
        if (isId && value is String && ObjectId.isValid(value)) {
            value = ObjectId(value);
        } else if (type.isEnum) {
            value = value.toString();
        } else if (type == LocalDateTime::class.java ||
                type == LocalDate::class.java) {
            value = value.AsLocalDateTime().AsDate()
        }
    }

    return Pair<String, Any?>(key.toString(), value);
}

infix fun MongoColumnName.match_size(value: Int): Criteria {
    return Criteria.where(this.toString()).size(value);
}


/**
 * 模糊查询，用法： mor.code.qrCodeInfo.product.name match_pattern "^.*王.*$"
 */
infix fun MongoColumnName.match_pattern(pattern: String): Criteria {
    return Criteria.where(this.toString()).regex(Pattern.compile(pattern, Pattern.CASE_INSENSITIVE))
}

infix fun MongoColumnName.match_like(like: String): Criteria {
    return this match_pattern "${like}"
}

infix fun MongoColumnName.match_not_equal(value: Any?): Criteria {
    var (key, to) = proc_mongo_match(this, value);
    return Criteria.where(key).`ne`(to)
}

infix fun MongoColumnName.match_dbColumn(to: MongoColumnName): Criteria {
    var (key, to) = proc_mongo_match(this, to);

    var d2 = BasicDBList();
    d2.add("$" + key)
    d2.add("$" + to)

    var dict = BasicBSONObject()
    dict.put("\$eq", d2)

    return Criteria.where("$" + "expr").`is`(dict);
}

/* mongo 3.4
infix fun String.match_dbColumn_3_4(to: Any?): Criteria {
    var (key, to) = proc_mongo_match(this, to);

    return Criteria.where("$" + "where").`is`("this.${key} == this.${to}");// Pair<String, T>(this, to);
}
*/

infix fun String.match(to: Any?): Criteria {
    return MongoColumnName(this).match(to)
}

infix fun MongoColumnName.match(to: Any?): Criteria {
    var (key, to) = proc_mongo_match(this, to);

    return Criteria.where(key).`is`(to);// Pair<String, T>(this, to);
}

//array_all
infix fun MongoColumnName.match_all(to: Array<*>): Criteria {
    var key = proc_mongo_key(this)
    var tos = mutableListOf<Any?>()
    to.forEach {
        var (key1, to1) = proc_mongo_match(this, it);
        tos.add(to1);
    }
    return Criteria.where(key).`all`(*tos.toTypedArray());
}

//infix fun <T> String.match_like(to: T): Criteria {
//
//    return Criteria.where(this).`alike`(to);// Pair<String, T>(this, to);
//}


infix fun MongoColumnName.match_type(to: MongoTypeEnum): Criteria {
    var (key, _) = proc_mongo_match(this, to);

    return Criteria.where(key).`type`(to.value);// Pair<String, T>(this, to);
}


infix fun MongoColumnName.match_gte(to: Any): Criteria {
    var (key, to) = proc_mongo_match(this, to);
    return Criteria.where(key).gte(to!!);
}

infix fun MongoColumnName.match_lte(to: Any): Criteria {
    var (key, to) = proc_mongo_match(this, to);
    return Criteria.where(key).lte(to!!);
}

infix fun MongoColumnName.match_greaterThan(to: Any): Criteria {
    var (key, to) = proc_mongo_match(this, to);
    return Criteria.where(key).gt(to!!);
}

infix fun MongoColumnName.match_lessThan(to: Any): Criteria {
    var (key, to) = proc_mongo_match(this, to);
    return Criteria.where(key).lt(to!!);
}

//大于等于并且小于。
infix fun MongoColumnName.match_between(value: Pair<Any, Any>): Criteria {
    var (key, from) = proc_mongo_match(this, value.first);
    var (key1, to) = proc_mongo_match(this, value.second);

    var dict = BasicBSONObject()
    dict.put("\$gte", from)
    dict.put("\$lt", to)
    return Criteria.where(key).`is`(dict)
    //return Criteria.where(key).gte(from).andOperator(Criteria.where(key).lt(to))
}

infix fun MongoColumnName.match_in(to: Collection<*>): Criteria {
    return this.match_in(to.toTypedArray())
}

//db.test1.find({"age":{"$in":['值1','值2',.....]}})
infix fun MongoColumnName.match_in(to: Array<*>): Criteria {
    var key = proc_mongo_key(this)
    var tos = mutableListOf<Any?>()
    to.forEach {
        var (key1, to1) = proc_mongo_match(this, it);
        tos.add(to1);
    }
    return Criteria.where(key).`in`(*tos.toTypedArray());
}

infix fun MongoColumnName.match_notin(to: Array<*>): Criteria {
    var key = proc_mongo_key(this);
    var tos = mutableListOf<Any?>()
    to.forEach {
        var (key1, to1) = proc_mongo_match(this, it);
        tos.add(to1);
    }
    return Criteria.where(key).`nin`(*tos.toTypedArray());
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
    var (key) = proc_mongo_match(this, null);
    return Criteria.where(key).`exists`(value);
}

/**
 * 用于 match
 */
infix fun MongoColumnName.match_elemMatch(value: Criteria): Criteria {
    var (key) = proc_mongo_match(this, null);
    return Criteria.where(key).`elemMatch`(value);
}

///**
// * 用于 project
// */
//infix fun String.match_filter(value: Criteria): BasicDBObject {
//    var key = this;
//    return Criteria.where(key).`filter`(value);
//}


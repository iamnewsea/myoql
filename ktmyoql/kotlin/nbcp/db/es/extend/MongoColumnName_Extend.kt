//package nbcp.db.es
//
//import nbcp.comm.*
//import nbcp.db.IdName
//import nbcp.comm.*
//import nbcp.db.es.*
//import java.time.LocalDate
//import java.time.LocalDateTime
//import java.util.regex.Pattern
//
///**
// * Created by udi on 17-7-10.
// */
//
//private fun proc_es_match(key: EsColumnName, value: Any?): Pair<String, Any?> {
//    var key = key
//    var keyString = key.toString();
//    var isId = false;
//    if (keyString == "id") {
//        key = EsColumnName("_id")
//        isId = true;
//    } else if (keyString == "_id") {
//        isId = true;
//    } else if (keyString.endsWith(".id")) {
//        key = EsColumnName(keyString.slice(0..keyString.length - 4) + "._id")
//        isId = true;
//    } else if (keyString.endsWith("._id")) {
//        isId = true;
//    }
//
//    var value = value;
//    if (value != null) {
//        var type = value::class.java
//        if (type.isEnum) {
//            value = value.toString();
//        } else if (type == LocalDateTime::class.java ||
//                type == LocalDate::class.java) {
//            value = value.AsLocalDateTime().AsDate()
//        } else if (isId) {
//            if (value is String) {
//                if (ObjectId.isValid(value)) {
//                    value = ObjectId(value);
//                }
//            } else if (type.isArray) {
//                value = (value as Array<*>).map {
//                    if (it is String && ObjectId.isValid(it)) {
//                        return@map ObjectId(it)
//                    }
//
//                    return@map it
//                }.toTypedArray()
//            } else if (type.IsListType()) {
//                value = (value as List<*>).map {
//                    if (it is String && ObjectId.isValid(it)) {
//                        return@map ObjectId(it)
//                    }
//
//                    return@map it
//                }
//            } else if (value is Pair<*, *>) {
//                var v1 = value.first;
//                if (v1 is String && ObjectId.isValid(v1)) {
//                    v1 = ObjectId(v1)
//                }
//
//                var v2 = value.second;
//                if (v2 is String && ObjectId.isValid(v2)) {
//                    v2 = ObjectId(v2)
//                }
//                value = Pair<Any?, Any?>(v1, v2);
//            }
//        }
//    }
//
//    return Pair<String, Any?>(key.toString(), value);
//}
//
//infix fun EsColumnName.match_size(value: Int): Criteria {
//    return Criteria.where(this.toString()).size(value);
//}
//
//
///**
// * 模糊查询，用法： mor.code.qrCodeInfo.product.name match_pattern "^.*王.*$"
// */
//infix fun EsColumnName.match_pattern(pattern: String): Criteria {
//    return Criteria.where(this.toString()).regex(Pattern.compile(pattern, Pattern.CASE_INSENSITIVE))
//}
//
//infix fun EsColumnName.match_like(like: String): Criteria {
//    return this match_pattern "${like}"
//}
//
//infix fun EsColumnName.match_not_equal(value: Any?): Criteria {
//    var (key, to) = proc_es_match(this, value);
//    return Criteria.where(key).`ne`(to)
//}
//
//
///* es 3.4
//infix fun String.match_dbColumn_3_4(to: Any?): Criteria {
//    var (key, to) = proc_es_match(this, to);
//
//    return Criteria.where("$" + "where").`is`("this.${key} == this.${to}");// Pair<String, T>(this, to);
//}
//*/
//
//infix fun String.match(to: Any?): Criteria {
//    return EsColumnName(this).match(to)
//}
//
//infix fun EsColumnName.match(to: Any?): Criteria {
//    var (key, to) = proc_es_match(this, to);
//
//    return Criteria.where(key).`is`(to);// Pair<String, T>(this, to);
//}
//
////array_all
//infix fun EsColumnName.match_all(to: Array<*>): Criteria {
//    var (key, tos) = proc_es_match(this, to)
//
//    return Criteria.where(key).`all`(*(tos as Array<*>));
//}
//
////infix fun <T> String.match_like(to: T): Criteria {
////
////    return Criteria.where(this).`alike`(to);// Pair<String, T>(this, to);
////}
//
//
//infix fun EsColumnName.match_type(to: EsTypeEnum): Criteria {
//    var (key, _) = proc_es_match(this, to);
//
//    return Criteria.where(key).`type`(to.value);// Pair<String, T>(this, to);
//}
//
//
//infix fun EsColumnName.match_gte(to: Any): Criteria {
//    var (key, to) = proc_es_match(this, to);
//    return Criteria.where(key).gte(to!!);
//}
//
//infix fun EsColumnName.match_lte(to: Any): Criteria {
//    var (key, to) = proc_es_match(this, to);
//    return Criteria.where(key).lte(to!!);
//}
//
//infix fun EsColumnName.match_greaterThan(to: Any): Criteria {
//    var (key, to) = proc_es_match(this, to);
//    return Criteria.where(key).gt(to!!);
//}
//
//infix fun EsColumnName.match_lessThan(to: Any): Criteria {
//    var (key, to) = proc_es_match(this, to);
//    return Criteria.where(key).lt(to!!);
//}
//
////大于等于并且小于。
//infix fun EsColumnName.match_between(value: Pair<Any, Any>): Criteria {
//    var (key, value2) = proc_es_match(this, value);
//    var pair = value2 as Pair<Any, Any>
//
//    var dict = BasicBSONObject()
//    dict.put("\$gte", pair.first)
//    dict.put("\$lt", pair.second)
//    return Criteria.where(key).`is`(dict)
//    //return Criteria.where(key).gte(from).andOperator(Criteria.where(key).lt(to))
//}
//
//infix fun EsColumnName.match_in(to: Collection<*>): Criteria {
//    return this.match_in(to.toTypedArray())
//}
//
////db.test1.find({"age":{"$in":['值1','值2',.....]}})
//infix fun EsColumnName.match_in(to: Array<*>): Criteria {
//    var (key, tos) = proc_es_match(this, to)
//
//    return Criteria.where(key).`in`(*(tos as Array<*>));
//}
//
//infix fun EsColumnName.match_notin(to: Array<*>): Criteria {
//    var (key, tos) = proc_es_match(this, to)
//    return Criteria.where(key).`nin`(*(tos as Array<*>));
//}
//
//infix fun EsColumnName.match_notin(to: Collection<*>): Criteria {
//    return this.match_notin(to.toTypedArray())
//}
//
///**
// * 用法：
// * 判断数组没有值，好处理： tags match_size 0
// * 判断数组有值,转化为：第一元素是否存在  EsColumnName("tags.0")  match_exists true
// */
//infix fun EsColumnName.match_exists(value: Boolean): Criteria {
//    var (key) = proc_es_match(this, null);
//    return Criteria.where(key).`exists`(value);
//}
//
///**
// * field match_hasValue true  => field exists  and field != null
// * field match_hasValue false  => field not exists  or field == null
// */
//infix fun EsColumnName.match_hasValue(value: Boolean): Criteria {
//    if (value) {
//        return this.match_exists(true).match_and(this.match_not_equal(null));
//    }
//
//    return return this.match_exists(false).match_or(this.match(null));
//}
//
//
///**
// * 用于 match
// */
//infix fun EsColumnName.match_elemMatch(value: Criteria): Criteria {
//    var (key) = proc_es_match(this, null);
//    return Criteria.where(key).`elemMatch`(value);
//}
//
/////**
//// * 用于 project
//// */
////infix fun String.match_filter(value: Criteria): BasicDBObject {
////    var key = this;
////    return Criteria.where(key).`filter`(value);
////}
//
//
//fun String.toOIdJson(): JsonMap {
////    if(ObjectId.isValid(this) == false)
//    return JsonMap("\$oid" to this)
//}
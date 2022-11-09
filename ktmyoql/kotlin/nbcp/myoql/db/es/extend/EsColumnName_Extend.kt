package nbcp.myoql.db.es.extend

import nbcp.base.comm.*;
import nbcp.base.db.*;
import nbcp.base.enums.*;
import nbcp.base.extend.*;
import nbcp.base.utils.*;
import nbcp.myoql.db.enums.*
import nbcp.myoql.db.*;
import nbcp.myoql.db.es.*
import nbcp.myoql.db.es.base.EsResultMsg
import org.elasticsearch.client.Response

/**
 * Created by udi on 17-7-10.
 */



fun Response.getResultMsg(): EsResultMsg {
    var result_map = this.entity.content.readBytes().toString(const.utf8).FromJson<JsonMap>();
    var ret = EsResultMsg();
    if (result_map == null) return ret;
    ret.error = result_map.get("errors").AsBoolean()
    ret.took = result_map.get("took").AsInt()

    var items = result_map.get("items") as List<Any>;
    if (!items.any()) {
        return ret;
    }

    var item = items.first() as Map<String, Any>;
    var item_keys = item.keys;
    if (!item_keys.any()) {
        return ret;
    }

    var item_key1 = item.keys.first().AsString();
    var item_value1 = item[item_key1] as Map<String, Any>
    ret.action = item_key1;
    ret.index = item_value1.get("_index").AsString()
    ret.id = item_value1.get("_id").AsString()
    ret.status = item_value1.get("status").AsInt()

    var error = item_value1.get("error") as Map<String, Any>?;
    if (error == null || !error.keys.any()) {
        return ret;
    }

    ret.type = error.get("type").AsString()
    ret.msg = error.get("reason").AsString()

    return ret;
}

//infix fun EsColumnName.match_size(value: Int): WhereData {
//    return WhereData.eq(this.toString()).size(value);
//}
//
//
///**
// * 模糊查询，用法： mor.code.qrCodeInfo.product.name match_pattern "^.*王.*$"
// */
//infix fun EsColumnName.match_pattern(pattern: String): WhereData {
//    return WhereData.where(this.toString()).regex(Pattern.compile(pattern, Pattern.CASE_INSENSITIVE))
//}
//
//infix fun EsColumnName.match_like(like: String): WhereData {
//    return this match_pattern "${like}"
//}
//
//infix fun EsColumnName.match_not_equal(value: Any?): WhereData {
//    var to  = proc_es_match( value);
//    return WhereData.where(key).`ne`(to)
//}


/* es 3.4
infix fun String.match_dbColumn_3_4(to: Any?): WhereData {
    var (key, to) = proc_es_match(this, to);

    return WhereData.where("$" + "where").`is`("this.${key} == this.${to}");// Pair<String, T>(this, to);
}
*/

//
////array_all
//infix fun EsColumnName.match_all(to: Array<*>): WhereData {
//    var (key, tos) = proc_es_match(this, to)
//
//    return WhereData.where(key).`all`(*(tos as Array<*>));
//}
//
////infix fun <T> String.match_like(to: T): WhereData {
////
////    return WhereData.where(this).`alike`(to);// Pair<String, T>(this, to);
////}
//
//
//infix fun EsColumnName.match_type(to: EsTypeEnum): WhereData {
//    var (key, _) = proc_es_match(this, to);
//
//    return WhereData.where(key).`type`(to.value);// Pair<String, T>(this, to);
//}
//
//
//infix fun EsColumnName.match_gte(to: Any): WhereData {
//    var (key, to) = proc_es_match(this, to);
//    return WhereData.where(key).gte(to!!);
//}
//
//infix fun EsColumnName.match_lte(to: Any): WhereData {
//    var (key, to) = proc_es_match(this, to);
//    return WhereData.where(key).lte(to!!);
//}
//
//infix fun EsColumnName.match_greaterThan(to: Any): WhereData {
//    var (key, to) = proc_es_match(this, to);
//    return WhereData.where(key).gt(to!!);
//}
//
//infix fun EsColumnName.match_lessThan(to: Any): WhereData {
//    var (key, to) = proc_es_match(this, to);
//    return WhereData.where(key).lt(to!!);
//}
//
////大于等于并且小于。
//infix fun EsColumnName.match_between(value: Pair<Any, Any>): WhereData {
//    var (key, value2) = proc_es_match(this, value);
//    var pair = value2 as Pair<Any, Any>
//
//    var dict = BasicBSONObject()
//    dict.put("\$gte", pair.first)
//    dict.put("\$lt", pair.second)
//    return WhereData.where(key).`is`(dict)
//    //return WhereData.where(key).gte(from).andOperator(WhereData.where(key).lt(to))
//}
//
//infix fun EsColumnName.match_in(to: Collection<*>): WhereData {
//    return this.match_in(to.toTypedArray())
//}
//
////db.test1.find({"age":{"$in":['值1','值2',.....]}})
//infix fun EsColumnName.match_in(to: Array<*>): WhereData {
//    var (key, tos) = proc_es_match(this, to)
//
//    return WhereData.where(key).`in`(*(tos as Array<*>));
//}
//
//infix fun EsColumnName.match_notin(to: Array<*>): WhereData {
//    var (key, tos) = proc_es_match(this, to)
//    return WhereData.where(key).`nin`(*(tos as Array<*>));
//}
//
//infix fun EsColumnName.match_notin(to: Collection<*>): WhereData {
//    return this.match_notin(to.toTypedArray())
//}
//
///**
// * 用法：
// * 判断数组没有值，好处理： tags match_size 0
// * 判断数组有值,转化为：第一元素是否存在  EsColumnName("tags.0")  match_exists true
// */
//infix fun EsColumnName.match_exists(value: Boolean): WhereData {
//    var (key) = proc_es_match(this, null);
//    return WhereData.where(key).`exists`(value);
//}
//
///**
// * field match_hasValue true  => field exists  and field != null
// * field match_hasValue false  => field not exists  or field == null
// */
//infix fun EsColumnName.match_hasValue(value: Boolean): WhereData {
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
//infix fun EsColumnName.match_elemMatch(value: WhereData): WhereData {
//    var (key) = proc_es_match(this, null);
//    return WhereData.where(key).`elemMatch`(value);
//}
//
/////**
//// * 用于 project
//// */
////infix fun String.match_filter(value: WhereData): BasicDBObject {
////    var key = this;
////    return WhereData.where(key).`filter`(value);
////}
//
//
//fun String.toOIdJson(): JsonMap {
////    if(ObjectId.isValid(this) == false)
//    return JsonMap("\$oid" to this)
//}
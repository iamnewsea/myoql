//package nbcp.db.es
//
//import nbcp.comm.*
//import nbcp.db.*
//import nbcp.base.extend.*
//import java.lang.reflect.ParameterizedType
//import java.time.LocalDate
//import java.time.LocalDateTime
//import java.util.regex.Pattern
//
///**
// * Created by udi on 17-7-10.
// */
///**
// * 另一种形式的条件。值可以是字段。
// */
//fun EsColumnName.match_expr (op:String, to: EsColumnName): Criteria {
//    var d2 = BasicDBList();
//    d2.add("$" + this.toString())
//    d2.add("$" + to)
//
//    var dict = BasicBSONObject()
//    dict.put("$" + op, d2)
//
//    return Criteria.where("$" + "expr").`is`(dict);
//}
//
//infix fun EsColumnName.match_expr_equal(to: EsColumnName): Criteria {
//    return this.match_expr("eq",to);
//}
//
//
//infix fun EsColumnName.match_expr_not_equal(to: EsColumnName): Criteria {
//    return this.match_expr("ne",to);
//}
//
//infix fun EsColumnName.match_expr_gte(to: EsColumnName): Criteria {
//    return this.match_expr("gte",to);
//}
//
//infix fun EsColumnName.match_expr_lte(to: EsColumnName): Criteria {
//    return this.match_expr("lte",to);
//}
//
//
//infix fun EsColumnName.match_expr_greaterThan(to: EsColumnName): Criteria {
//    return this.match_expr("gt",to);
//}
//
//infix fun EsColumnName.match_expr_lessThan(to: EsColumnName): Criteria {
//    return this.match_expr("lt",to);
//}
//
//infix fun EsColumnName.match_expr_between(to: EsColumnName): Criteria {
//    return this.match_expr("between",to);
//}
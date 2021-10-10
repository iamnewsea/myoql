@file:JvmName("MyOqlSql")
@file:JvmMultifileClass

package nbcp.db.sql

import nbcp.comm.*
import nbcp.db.sql.*
import nbcp.db.mysql.*
import java.io.Serializable
import java.util.*

/**
 * Created by udi on 17-7-10.
 */
fun proc_value(value: Any): Any {
    var type = value::class.java
    if (type.isEnum) {
        return value.toString();
    } else if (type == UUID::class.java) {
        return value.toString()
    } else if (type == Boolean::class.java || type == java.lang.Boolean::class.java) {
        if (value.AsBoolean()) return 1;
        else return 0;
    }

    return value
}

//infix fun WhereData.db_and(to: WhereData): WhereData {
//    if (to.hasValue == false) return this;
//    if (this.hasValue == false) return to
//    this.and(to)
//    return this;
//}
//
//infix fun WhereData.db_or(to: WhereData): WhereData {
//    if (to.hasValue == false) return this;
//    if (this.hasValue == false) return to
//
//    var where = WhereData();
//    where.or(to)
//    return where;
//}

private fun SqlColumnName.column_match_value(op: String, value: Serializable): WhereData {
    var value = proc_value(value);
    return WhereData("${this.fullName} ${op} {${this.jsonKeyName}}", JsonMap("${this.jsonKeyName}" to value))
}

/**
 * like 操作
 * @param value: 可以包含合法的 %,_
 */
infix fun SqlColumnName.like(value: String): WhereData = this.column_match_value("like", value)

/**
 * like "%查询内容%"
 */
infix fun SqlColumnName.like_all(value: String): WhereData = this.column_match_value("like", ("%" + value + "%"))


//infix fun SqlColumnName.match(value: Serializable): WhereData = this.column_match_value("=", value)
//infix fun SqlColumnName.match(value: String): WhereData = this.column_match_value("=", value)
//infix fun SqlColumnName.match(value: Boolean): WhereData = this.column_match_value("=", value)
//infix fun SqlColumnName.match(value: LocalDate): WhereData = this.column_match_value("=", value)
//infix fun SqlColumnName.match(value: LocalDateTime): WhereData = this.column_match_value("=", value)

/**
 * 相等操作
 */
infix fun SqlColumnName.match(value: Serializable): WhereData {
    if (value is SqlColumnName) {
        return WhereData("${this.fullName} = ${value.fullName}")
    }
    return this.column_match_value("=", value);
}


//infix fun SqlColumnName.match_not_equal(value: Number): WhereData = this.column_match_value("!=", value)
//infix fun SqlColumnName.match_not_equal(value: String): WhereData = this.column_match_value("!=", value)
//infix fun SqlColumnName.match_not_equal(value: Boolean): WhereData = this.column_match_value("!=", value)
//infix fun SqlColumnName.match_not_equal(value: LocalDate): WhereData = this.column_match_value("!=", value)
//infix fun SqlColumnName.match_not_equal(value: LocalDateTime): WhereData = this.column_match_value("!=", value)

/**
 * 不等操作
 */
infix fun SqlColumnName.match_not_equal(value: Serializable): WhereData {
    if (value is SqlColumnName) {
        return WhereData("${this.fullName} != ${value.fullName}")
    }
    return this.column_match_value("!=", value);
}

//infix fun SqlColumnName.match_gte(value: Number): WhereData = this.column_match_value(">=", value)
//infix fun SqlColumnName.match_gte(value: String): WhereData = this.column_match_value(">=", value)
//infix fun SqlColumnName.match_gte(value: LocalDate): WhereData = this.column_match_value(">=", value)
//infix fun SqlColumnName.match_gte(value: LocalDateTime): WhereData = this.column_match_value(">=", value)

/**
 * 大于等于操作
 */
infix fun SqlColumnName.match_gte(value: Serializable): WhereData {
    if (value is SqlColumnName) {
        return WhereData("${this.fullName} >= ${value.fullName}")
    }
    return this.column_match_value(">=", value)
}


//infix fun SqlColumnName.match_greaterThan(value: Number): WhereData = this.column_match_value(">", value)
//infix fun SqlColumnName.match_greaterThan(value: String): WhereData = this.column_match_value(">", value)
//infix fun SqlColumnName.match_greaterThan(value: LocalDate): WhereData = this.column_match_value(">", value)
//infix fun SqlColumnName.match_greaterThan(value: LocalDateTime): WhereData = this.column_match_value(">", value)

/**
 * 大于操作，不包含等于
 */
infix fun SqlColumnName.match_greaterThan(value: Serializable): WhereData {
    if (value is SqlColumnName) {
        return WhereData("${this.fullName} > ${value.fullName}")
    }
    return this.column_match_value(">", value);
}


//infix fun SqlColumnName.match_lte(value: Number): WhereData = this.column_match_value("<=", value)
//infix fun SqlColumnName.match_lte(value: String): WhereData = this.column_match_value("<=", value)
//infix fun SqlColumnName.match_lte(value: LocalDate): WhereData = this.column_match_value("<=", value)
//infix fun SqlColumnName.match_lte(value: LocalDateTime): WhereData = this.column_match_value("<=", value)

/**
 * 小于等于操作。
 */
infix fun SqlColumnName.match_lte(value: Serializable): WhereData {
    if (value is SqlColumnName) {
        return WhereData("${this.fullName} <= ${value.fullName}")
    }

    return this.column_match_value("<=", value)
}


//infix fun SqlColumnName.match_lessThan(value: Number): WhereData = this.column_match_value("<", value)
//infix fun SqlColumnName.match_lessThan(value: String): WhereData = this.column_match_value("<", value)
//infix fun SqlColumnName.match_lessThan(value: LocalDate): WhereData = this.column_match_value("<", value)
//infix fun SqlColumnName.match_lessThan(value: LocalDateTime): WhereData = this.column_match_value("<", value)
/**
 * 小于操作，不包含等于
 */
infix fun SqlColumnName.match_lessThan(value: Serializable): WhereData {
    if (value is SqlColumnName) {
        return WhereData("${this.fullName} < ${value.fullName}")
    }
    return this.column_match_value("<", value)
}


private fun SqlColumnName.column_match_between(min: Any, max: Any): WhereData {
    var minValue = proc_value(min);
    var maxValue = proc_value(max);

    return WhereData("${this.fullName} >= {${this.jsonKeyName}_min} and ${this.fullName} < {${this.jsonKeyName}_max}", JsonMap("${this.jsonKeyName}_min" to minValue, "${this.jsonKeyName}_max" to maxValue));
}

//fun SqlColumnName.match_between(min: Number, max: Number): WhereData = this.column_match_between(min, max)
//fun SqlColumnName.match_between(min: String, max: String): WhereData = this.column_match_between(min, max)
//fun SqlColumnName.match_between(min: LocalDate, max: LocalDate): WhereData = this.column_match_between(min, max)
//fun SqlColumnName.match_between(min: LocalDateTime, max: LocalDateTime): WhereData = this.column_match_between(min, max)

/**
 * 大于等于，并且 小于
 */
fun <T : Serializable> SqlColumnName.match_between(min: T, max: T): WhereData {
    if (min is SqlColumnName && max is SqlColumnName) {
        return WhereData("${this.fullName} >= ${min.fullName} and ${this.fullName} < ${max.fullName}")
    }
    return this.column_match_between(min, max);
}


//private inline fun SqlColumnName.column_match_some(op: String, values: Array<out Serializable>): WhereData {
//    var needWrap = values.any { it != null && it::class.java == String::class.java }
//    var value = values
//            .map {
//                var v = proc_value(it)
//                if (needWrap) {
//                    return@map "'" + v + "'"
//                }
//                return@map v;
//            }
//            .joinToString(",")
//            .AsString("null")
//
//    return WhereData("${this.fullName} ${op} ( ${value} )");
//}

//infix fun SqlColumnName.match_in(values: Array<out Number>): WhereData = this.column_match_some("in", values)
//infix fun SqlColumnName.match_in(values: Array<String>): WhereData = this.column_match_some("in", values)
//infix fun SqlColumnName.match_in(values: Array<LocalDate>): WhereData = this.column_match_some("in", values)
//infix fun SqlColumnName.match_in(values: Array<LocalDateTime>): WhereData = this.column_match_some("in", values)

/**
 * in (values)操作
 */
infix inline fun <reified T : Serializable> SqlColumnName.match_in(values: Array<T>): WhereData {
    if (T::class.java == SqlColumnName::class.java) {
        return WhereData("${this.fullName} in (${values.map { (it as SqlColumnName).fullName }.joinToString(",").AsString("null")} )")
    }


    var needWrap = this.dbType.needTextWrap()
    var value = values
            .map {
                var v = proc_value(it)
                if (needWrap) {
                    return@map "'" + v + "'"
                }
                return@map v;
            }
            .joinToString(",")
            .AsString("null")

    return WhereData("${this.fullName} in ( ${value} )");
}

/**
 * in 子查询
 */
infix fun SqlColumnName.match_in(select: SqlQueryClip<*, *>): WhereData {
    var subSelect = select.toSql()
    var ret = WhereData("${this.fullName} in ( ${subSelect.expression} )")
    ret.values += subSelect.values
    return ret;
}

/**
 * not in (values) 操作
 */
infix inline fun <reified T : Serializable> SqlColumnName.match_not_in(values: Array<T>): WhereData {
    if (T::class.java == SqlColumnName::class.java) {
        return WhereData("${this.fullName} not in (${values.map { (it as SqlColumnName).fullName }.joinToString(",").AsString("null")} )")
    }


    var needWrap = this.dbType.needTextWrap()
    var value = values
            .map {
                var v = proc_value(it)
                if (needWrap) {
                    return@map "'" + v + "'"
                }
                return@map v;
            }
            .joinToString(",")
            .AsString("null")

    return WhereData("${this.fullName} not in ( ${value} )");
}

/**
 * not in 子查询
 */
infix fun SqlColumnName.match_not_in(select: SqlQueryClip<*, *>): WhereData {
    var subSelect = select.toSql()
    var ret = WhereData("${this.fullName} not in ( ${subSelect.expression} )")
    ret.values += subSelect.values
    return ret;
}

//
//infix fun SqlColumnName.match_not_in(values: Array<out Number>): WhereData = this.column_match_some("not in", values)
//infix fun SqlColumnName.match_not_in(values: Array<String>): WhereData = this.column_match_some("not in", values)
//infix fun SqlColumnName.match_not_in(values: Array<LocalDate>): WhereData = this.column_match_some("not in", values)
//infix fun SqlColumnName.match_not_in(values: Array<LocalDateTime>): WhereData = this.column_match_some("not in", values)
//infix fun SqlColumnName.match_not_in(values: Array<SqlColumnName>): WhereData {
//    return WhereData("${this.fullName} not in ( ${values.map { it.fullName }.joinToString(",").AsString("null")} )")
//}

//infix inline fun<reified T:Serializable> SqlColumnName.match_in(values:Collection<T>): SingleSqlData = this.column_match_some("in", values.toTypedArray())
//infix inline fun<reified T:Serializable> SqlColumnName.match_not_in(values:Collection<T>): SingleSqlData = this.column_match_some("not in",values.toTypedArray())

/**
 * 生成 (col is null or col = 0/'' )
 */
fun SqlColumnName.isNullOrEmpty(): WhereData {
    var emptyValue = "";
    if (this.dbType.isNumberic()) {
        emptyValue = " or ${this.fullName} = 0"
    } else if (this.dbType != DbType.Other) {
        emptyValue = " or ${this.fullName} = ''";
    }

    return WhereData("(${this.fullName} is null ${emptyValue})")
}
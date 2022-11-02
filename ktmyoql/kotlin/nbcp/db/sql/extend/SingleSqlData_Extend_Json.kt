@file:JvmName("MyOqlSql")
@file:JvmMultifileClass

package nbcp.db.sql

import nbcp.comm.*
import nbcp.db.db
import java.io.Serializable
import java.lang.RuntimeException


/**
 * 函数名称和数据库函数相同，可用于数组。
 */
fun BaseAliasSqlSect.json_length(): SqlParameterData {
    var ret = this.toSingleSqlData();
    ret.expression = "json_length(${ret.expression})"
    return ret;
}



/**
 * 判断是否包含，可用于数组。
 */
fun BaseAliasSqlSect.json_contains(jsonDoc: BaseAliasSqlSect): SqlParameterData {
    var ret = this.toSingleSqlData();
    ret.expression = "json_contains("
    ret += this.toSingleSqlData()
    ret.expression +=  ","
    ret += jsonDoc.toSingleSqlData()
    ret.expression += ")"
    return ret;
}

/**
 * 判断是否有交集，可用于数组。
 */
fun BaseAliasSqlSect.json_overlaps(jsonDoc: BaseAliasSqlSect): SqlParameterData {
    var ret = this.toSingleSqlData();
    ret.expression = "json_overlaps("
    ret += this.toSingleSqlData()
    ret.expression +=  ","
    ret += jsonDoc.toSingleSqlData()
    ret.expression += ")"
    return ret;
}

/**
 * 判断是否Json相同。
 */
fun BaseAliasSqlSect.json_equals(jsonDoc: BaseAliasSqlSect): SqlParameterData {
    var ret = this.toSingleSqlData();
    ret.expression = "json_equals("
    ret += this.toSingleSqlData()
    ret.expression +=  ","
    ret += jsonDoc.toSingleSqlData()
    ret.expression += ")"
    return ret;
}




/**
 * 判断是否包含，可用于数组。
 */
fun BaseAliasSqlSect.json_contains(jsonString: String): SqlParameterData {
    var ret = this.toSingleSqlData();
    ret.expression = "json_contains("
    ret += this.toSingleSqlData()
    ret.expression += (", '" + jsonString.replace("'","\\'") + "'")
    ret.expression += ")"
    return ret;
}

/**
 * 判断是否有交集，可用于数组。
 */
fun BaseAliasSqlSect.json_overlaps(jsonString: String): SqlParameterData {
    var ret = this.toSingleSqlData();
    ret.expression = "json_overlaps("
    ret += this.toSingleSqlData()
    ret.expression += (", '" + jsonString.replace("'","\\'") + "'")
    ret.expression += ")"
    return ret;
}

/**
 * 判断是否Json相同。
 */
fun BaseAliasSqlSect.json_equals(jsonString: String): SqlParameterData {
    var ret = this.toSingleSqlData();
    ret.expression = "json_equals("
    ret += this.toSingleSqlData()
    ret.expression += (", '" + jsonString.replace("'","\\'") + "'")
    ret.expression += ")"
    return ret;
}


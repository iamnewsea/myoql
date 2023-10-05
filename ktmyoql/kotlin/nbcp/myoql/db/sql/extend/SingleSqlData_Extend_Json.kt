@file:JvmName("MyOqlSql")
@file:JvmMultifileClass

package nbcp.myoql.db.sql.extend


import nbcp.myoql.db.sql.base.BaseAliasSqlSect
import nbcp.myoql.db.sql.base.SqlParameterData


/**
 * 函数名称和数据库函数相同，可用于数组。
 */
fun BaseAliasSqlSect.jsonLength(): SqlParameterData {
    var ret = this.toSingleSqlData();
    ret.expression = "json_length(${ret.expression})"
    return ret;
}



/**
 * 判断是否包含，可用于数组。
 */
fun BaseAliasSqlSect.jsonContains(jsonDoc: BaseAliasSqlSect): SqlParameterData {
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
fun BaseAliasSqlSect.jsonOverlaps(jsonDoc: BaseAliasSqlSect): SqlParameterData {
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
fun BaseAliasSqlSect.jsonEquals(jsonDoc: BaseAliasSqlSect): SqlParameterData {
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
fun BaseAliasSqlSect.jsonContains(jsonString: String): SqlParameterData {
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
fun BaseAliasSqlSect.jsonOverlaps(jsonString: String): SqlParameterData {
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
fun BaseAliasSqlSect.jsonEquals(jsonString: String): SqlParameterData {
    var ret = this.toSingleSqlData();
    ret.expression = "json_equals("
    ret += this.toSingleSqlData()
    ret.expression += (", '" + jsonString.replace("'","\\'") + "'")
    ret.expression += ")"
    return ret;
}


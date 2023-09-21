package nbcp.myoql.db.sql.base

import nbcp.base.comm.JsonMap
import nbcp.base.extend.CloneObject
import nbcp.base.extend.HasValue
import nbcp.base.utils.CodeUtil
import nbcp.myoql.db.db


open class SqlParameterData constructor() : BaseAliasSqlSect() {

    var expression: String = ""
        get() {
            if (this.aliaValue.HasValue) {
                return "${field} as ${db.sql.getSqlQuoteName(this.aliaValue)}"
            }
            return field;
        }
        set(value) {
            field = value;
        }

    var values: JsonMap = JsonMap();

    constructor(
            // 使用 冒号+变量名  :变量名 表示 变量
            expression: String,

            // JsonMap 的 key = corp_id
            values: JsonMap = JsonMap()
    ) : this() {
        this.expression = expression;
        this.values = values;
    }

    fun alias(alias: String): SqlParameterData {
        this.aliaValue = alias;
        return this;
    }


    override fun toSingleSqlData(): SqlParameterData {
        return this.CloneObject()
    }


    //
//    private fun getJsonKeysFromExpression(): Set<String> {
//        return """\{([^}]+)}""".toRegex(RegexOption.DOT_MATCHES_ALL)
//                .findAll(this.expression)
//                .map { it.groupValues.last() }
//                .toSet()
//    }


//    fun toExecuteSqlAndParameters(): SqlExecuteData {
//        var exp = this.expression;
//        var parameters = mutableListOf<SqlParameterData>()
//
//        """\{([^}]+)}""".toRegex(RegexOption.DOT_MATCHES_ALL)
//            .findAll(this.expression)
//            .sortedByDescending { it.range.start }
//            .forEach {
//                var key = it.groupValues[1]
////                    var key_var = it.groupValues[0]
//
//                exp = exp.Slice(0, it.range.start) + "?" + exp.substring(it.range.endInclusive + 1)
//
//                var value = this.values[key]
//                if (value == null) {
//                    parameters.add(SqlParameterData(String::class.java, null))
//                } else {
//                    parameters.add(SqlParameterData(this.values[key]!!::class.java, this.values[key]!!))
//                }
//            }
//
//
//        parameters.reverse()
//        return SqlExecuteData(exp, parameters.toTypedArray())
//    }


    operator fun plus(other: SqlParameterData): SqlParameterData {
        var other2 = SqlParameterData(other.expression, JsonMap(other.values));


        //去除 #@
        var sameKeys = this.values.keys.intersect(other2.values.keys)

        sameKeys.forEachIndexed { index, sameKey ->

            //SingleSqlData拼接Sql时，参数索引并不可靠，必须使用唯一参数名。
            var key = sameKey + "_" + CodeUtil.getCode();

            other2.expression = other2.expression.replace(":${sameKey}", ":${key}");
            other2.values.set(key, other2.values.get(sameKey));
            other2.values.remove(sameKey)
        }


        return SqlParameterData(this.expression + other2.expression, JsonMap(this.values + other2.values));
    }
//
//    operator fun plus(expression: String): SingleSqlData {
//        return SingleSqlData(this.expression + expression, JsonMap(this.values))
//    }
}


package nbcp.db.sql

import nbcp.comm.*
import nbcp.comm.*
import nbcp.base.extend.Slice
import nbcp.db.mysql.*
import java.io.Serializable
import java.util.LinkedHashSet

data class SingleSqlData(
        // 使用 {变量名} 表示 变量
        var expression: String = "",

        // JsonMap 的 key = corp_id
        var values: JsonMap = JsonMap()
) : Serializable {
    //
//    private fun getJsonKeysFromExpression(): Set<String> {
//        return """\{([^}]+)}""".toRegex(RegexOption.DOT_MATCHES_ALL)
//                .findAll(this.expression)
//                .map { it.groupValues.last() }
//                .toSet()
//    }


    //瘦身
    init {
//        values.onlyHoldKeys(getJsonKeysFromExpression())
    }

    fun toExecuteSqlAndParameters(): SqlExecuteData {
        var exp = this.expression;
        var parameters = mutableListOf<SqlParameterData>()

        """\{([^}]+)}""".toRegex(RegexOption.DOT_MATCHES_ALL)
                .findAll(this.expression)
                .sortedByDescending { it.range.start }
                .forEach {
                    var key = it.groupValues[1]
//                    var key_var = it.groupValues[0]

                    exp = exp.Slice(0, it.range.start) + "?" + exp.Slice(it.range.endInclusive + 1)

                    var value = this.values[key]
                    if (value == null) {
                        parameters.add(SqlParameterData(String::class.java, null))
                    } else {
                        parameters.add(SqlParameterData(this.values[key]!!::class.java, this.values[key]!!))
                    }
                }


        parameters.reverse()
        return SqlExecuteData(exp, parameters.toTypedArray())
    }


    operator fun plus(other: SingleSqlData): SingleSqlData {
        var other2 = SingleSqlData(other.expression, JsonMap(other.values));


        //去除 #@
        var sameKeys = this.values.keys.intersect(other2.values.keys)

        sameKeys.forEachIndexed { index, sameKey ->
            var key = sameKey + "_" + (index + 1);
            other2.expression = other2.expression.replace("{${sameKey}}", "{${key}}");
            other2.values.set(key, other2.values.get(sameKey));
            other2.values.remove(sameKey)
        }


        return SingleSqlData(this.expression + other2.expression, JsonMap(this.values + other2.values));
    }
//
//    operator fun plus(expression: String): SingleSqlData {
//        return SingleSqlData(this.expression + expression, JsonMap(this.values))
//    }
}


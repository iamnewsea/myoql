package nbcp.myoql.db.sql.base

import nbcp.base.comm.JsonMap
import nbcp.base.extend.AsString
import nbcp.base.extend.HasValue
import nbcp.base.extend.IsCollectionType
import nbcp.base.extend.ToJson
import nbcp.myoql.db.db
import nbcp.myoql.db.sql.component.SqlQueryClip
import nbcp.myoql.db.sql.component.WhereData
import nbcp.myoql.db.sql.enums.DbType
import nbcp.myoql.db.sql.extend.json_contains
import nbcp.myoql.db.sql.extend.json_equals
import nbcp.myoql.db.sql.extend.json_length
import nbcp.myoql.db.sql.extend.proc_value
import java.io.Serializable


open class SqlColumnName(
    val dbType: DbType,
    tableName: String,
    name: String
) : BaseAliasSqlSect() {
    /**
     * 表名
     */
    var tableName: String = tableName

    /**
     * 列名
     */
    var name: String = name


    private fun SqlColumnName.sqlMatch(op: String, value: Serializable): WhereData {
        var valueValue = proc_value(value);
        return WhereData("${this.fullName} ${op} :${this.paramVarKeyName}", JsonMap(this.paramVarKeyName to valueValue))
    }


    /**
     * like 操作
     * @param value: 可以包含合法的 %,_
     */
    infix fun like(value: String): WhereData = this.sqlMatch("like", value)

    /**
     * like "%查询内容%"
     */
    infix fun likeAll(value: String): WhereData = this.sqlMatch("like", ("%" + value + "%"))


    /**
     * 相等操作, 也可以比较 Json 数组的相等。
     */
    infix fun sqlEquals(value: Serializable): WhereData {
        if (value is SqlColumnName) {
            return WhereData("${this.fullName} = ${value.fullName}")
        }

        //仅支持 数组相等。
        if (this.dbType == DbType.JSON) {
            val json_equals = this.json_equals(value.ToJson())

            return WhereData(
                "${json_equals.expression} = 1",
                json_equals.values
            )
        }
        return this.sqlMatch("=", value);
    }

    /**
     * 相等操作, 也可以比较 Json 数组内容的相等。
     */
    infix fun sqlEqualsArrayContent(value: Serializable): WhereData {
        if (value is SqlColumnName) {
            return WhereData("${this.fullName} = ${value.fullName}")
        }

        //仅支持 数组相等。
        if (this.dbType == DbType.JSON) {
            val v_type = value::class.java
            val json_length = this.json_length()
            if (v_type.isArray) {
                var ary = value as Array<Any?>;
                val json_contains = this.json_contains(db.sql.json_array(ary.toList()))
                return WhereData(
                    "${json_length.expression} = ${ary.size} and ${json_contains.expression} = 1",
                    json_length.values + json_contains.values
                )
            } else if (v_type.IsCollectionType) {
                var ary = value as Collection<Any?>;
                val json_contains = this.json_contains(db.sql.json_array(ary))
                return WhereData(
                    "${json_length.expression} = ${ary.size} and ${json_contains.expression} = 1",
                    json_length.values + json_contains.values
                )
            } else {
                val json_contains = this.json_contains(db.sql.json_array(listOf(value)))
                return WhereData(
                    "${json_length.expression} = 1 or ${json_contains.expression} = 1",
                    json_length.values + json_contains.values
                )
            }
        }
        return this.sqlMatch("=", value);
    }

    infix fun sqlRegexp(value: String): WhereData {
        return this.sqlMatch("regexp", value);
    }

    /*
    * 不等操作
    */
    infix fun sqlNotEquals(value: Serializable): WhereData {
        if (value is SqlColumnName) {
            return WhereData("${this.fullName} != ${value.fullName}")
        }

        //仅支持 数组相等。
        if (this.dbType == DbType.JSON) {
            val json_equals = this.json_equals(value.ToJson())

            return WhereData(
                "${json_equals.expression} = 0",
                json_equals.values
            )
        }
        return this.sqlMatch("!=", value);
    }

    infix fun sqlJsonArrayContains(value: Serializable): WhereData {
        if (value is SqlColumnName) {
            return WhereData("${this.json_contains(value)} = 1")
        }

        //仅支持 数组相等。
        if (this.dbType == DbType.JSON) {
            val v_type = value::class.java

            if (v_type.isArray) {
                val ary = value as Array<Any?>;
                val json_contains = this.json_contains(db.sql.json_array(ary.toList()))
                return WhereData("${json_contains.expression} = 1", json_contains.values)
            } else if (v_type.IsCollectionType) {
                val ary = value as Collection<Any?>;
                val json_contains = this.json_contains(db.sql.json_array(ary))
                return WhereData("${json_contains.expression} = 1", json_contains.values)
            } else {
                val json_contains = this.json_contains(db.sql.json_array(listOf(value)))
                return WhereData("${json_contains.expression} = 1", json_contains.values)
            }
        }

        throw java.lang.RuntimeException("json_contains要求列必须是JSON类型！")
    }

    infix fun sqlJsonArrayNotContains(value: Serializable): WhereData {
        if (value is SqlColumnName) {
            return WhereData("${this.json_contains(value)} = 0")
        }

        //仅支持 数组相等。
        if (this.dbType == DbType.JSON) {
            val v_type = value::class.java
            if (v_type.isArray) {
                val ary = value as Array<Any?>;
                val json_contains = this.json_contains(db.sql.json_array(ary.toList()))
                return WhereData("${json_contains.expression} = 0")
            } else if (v_type.IsCollectionType) {
                val ary = value as Collection<Any?>;
                val json_contains = this.json_contains(db.sql.json_array(ary))
                return WhereData("${json_contains.expression} = 0")
            } else {
                val json_contains = this.json_contains(db.sql.json_array(listOf(value)))
                return WhereData("${json_contains.expression} = 0")
            }
        }

        throw java.lang.RuntimeException("json_contains要求列必须是JSON类型！")
    }


    infix fun sqlJsonObjectContains(value: Serializable): WhereData {
        if (value is SqlColumnName) {
            return WhereData("${this.json_contains(value)} = 1")
        }

        //仅支持 数组相等。
        if (this.dbType == DbType.JSON) {
            val json_contains = this.json_contains(value.ToJson())
            return WhereData("${json_contains.expression} = 1", json_contains.values)

        }

        throw java.lang.RuntimeException("json_contains要求列必须是JSON类型！")
    }

    infix fun sqlJsonObjectNotContains(value: Serializable): WhereData {
        if (value is SqlColumnName) {
            return WhereData("${this.json_contains(value)} = 0")
        }

        //仅支持 数组相等。
        if (this.dbType == DbType.JSON) {
            val json_contains = this.json_contains(value.ToJson())
            return WhereData("${json_contains.expression} = 0")
        }

        throw java.lang.RuntimeException("json_contains要求列必须是JSON类型！")
    }

    /**
     * 大于等于操作
     */
    infix fun sqlGreaterThanEquals(value: Serializable): WhereData {
        if (value is SqlColumnName) {
            return WhereData("${this.fullName} >= ${value.fullName}")
        }
        return this.sqlMatch(">=", value)
    }

    /**
     * 大于操作，不包含等于
     */
    infix fun sqlGreaterThan(value: Serializable): WhereData {
        if (value is SqlColumnName) {
            return WhereData("${this.fullName} > ${value.fullName}")
        }
        return this.sqlMatch(">", value);
    }

    /**
     * 小于等于操作。
     */
    infix fun sqlLessThanEquals(value: Serializable): WhereData {
        if (value is SqlColumnName) {
            return WhereData("${this.fullName} <= ${value.fullName}")
        }

        return this.sqlMatch("<=", value)
    }

    /**
     * 小于操作，不包含等于
     */
    infix fun sqlLessThan(value: Serializable): WhereData {
        if (value is SqlColumnName) {
            return WhereData("${this.fullName} < ${value.fullName}")
        }
        return this.sqlMatch("<", value)
    }


    private fun sqlUntil(min: Any, max: Any): WhereData {
        var minValue = proc_value(min);
        var maxValue = proc_value(max);

        return WhereData(
            "${this.fullName} >= :${this.paramVarKeyName}_min and ${this.fullName} < :${this.paramVarKeyName}_max",
            JsonMap("${this.paramVarKeyName}_min" to minValue, "${this.paramVarKeyName}_max" to maxValue)
        );
    }

    /**
     * 开闭区间，表示大于等于 并且 小于
     */
    fun <T : Serializable> sqlUntil(min: T, max: T): WhereData {
        if (min is SqlColumnName && max is SqlColumnName) {
            return WhereData("${this.fullName} >= ${min.fullName} and ${this.fullName} < ${max.fullName}")
        }
        return this.sqlUntil(min, max);
    }


    /**
     * in (values)操作
     */
    infix inline fun <reified T : Serializable> sqlIn(values: Array<T>): WhereData {
        if (T::class.java == SqlColumnName::class.java) {
            return WhereData(
                "${this.fullName} in (${
                    values.map { (it as SqlColumnName).fullName }.joinToString(",").AsString("null")
                } )"
            )
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
    infix fun SqlColumnName.sqlIn(select: SqlQueryClip<*, *>): WhereData {
        var subSelect = select.toSql()
        var ret = WhereData("${this.fullName} in ( ${subSelect.expression} )")
        ret.values += subSelect.values
        return ret;
    }

    /**
     * not in (values) 操作
     */
    infix inline fun <reified T : Serializable> sqlNotIn(values: Array<T>): WhereData {
        if (T::class.java == SqlColumnName::class.java) {
            return WhereData(
                "${this.fullName} not in (${
                    values.map { (it as SqlColumnName).fullName }.joinToString(",").AsString("null")
                } )"
            )
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
    infix fun sqlNotIn(select: SqlQueryClip<*, *>): WhereData {
        var subSelect = select.toSql()
        var ret = WhereData("${this.fullName} not in ( ${subSelect.expression} )")
        ret.values += subSelect.values
        return ret;
    }


    /**
     * 生成 (col is null or col = 0/'' )
     */
    fun isNullOrEmpty(): WhereData {
        var emptyValue = "";
        if (this.dbType.isNumberic()) {
            emptyValue = " or ${this.fullName} = 0"
        } else if (this.dbType != DbType.OTHER) {
            emptyValue = " or ${this.fullName} = ''";
        }

        return WhereData("(${this.fullName} is null ${emptyValue})")
    }


    companion object {
        @JvmStatic
        fun of(name: String): SqlColumnName {
            return SqlColumnName(DbType.OTHER, "", name)
        }

        @JvmStatic
        fun of(dbType: DbType, name: String): SqlColumnName {
            return SqlColumnName(dbType, "", name)
        }
    }


    open val fullName: String
        get() {
            if (this.tableName.HasValue) {
                return "`${this.tableName}`.`${this.name}`"
            }

            //按常数列, 函数列,表达式来对待
            return "${this.name}"
        }

    //用于 json 中的 key
    //变量，必须是  {s_corp_name}
    open val paramVarKeyName: String
        get() {
            if (aliaValue.HasValue) return this.aliaValue

            if (this.tableName.HasValue) {
                return "${this.tableName}_${this.name}"
            }

            //按常数列, 函数列,表达式来对待
            return "${this.name}"
        }

    fun alias(alias: String): SqlColumnName {
        if (alias == this.name) {
            this.aliaValue = ""
            return this;
        }

        val ret = SqlColumnName(dbType, tableName, name);
        ret.aliaValue = alias;
        return ret;
    }

    /**
     * 返回 columnAliaValue.AsString( name )
     */
    override fun getAliasName(): String = this.aliaValue.AsString(this.name)


    override fun toSingleSqlData(): SqlParameterData {
        var ret = SqlParameterData()
        if (this.aliaValue.HasValue && this.aliaValue != this.name) {
            ret.aliaValue = this.aliaValue;
        }
        ret.expression = this.fullName

        return ret;
    }

    override fun toString(): String {
        return name
    }

    override fun equals(other: Any?): Boolean {
        //地址。
        if (super.equals(other)) return true;

        if (other == null) return false;
        if (other is SqlColumnName) {
            return this.dbType == other.dbType && this.tableName == other.tableName && this.name == other.name && this.aliaValue == other.aliaValue
        }
        return false;
    }

    fun toArray(): SqlColumnNames {
        return SqlColumnNames(this);
    }
}

fun Collection<SqlColumnName>.toArray(): SqlColumnNames {
    return SqlColumnNames(*this.toTypedArray())
}
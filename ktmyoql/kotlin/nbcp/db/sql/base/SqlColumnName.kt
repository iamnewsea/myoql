package nbcp.db.sql

import nbcp.comm.AsString
import nbcp.comm.HasValue
import nbcp.comm.IsCollectionType
import nbcp.comm.JsonMap
import nbcp.db.db
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


    private fun SqlColumnName.column_match_value(op: String, value: Serializable): WhereData {
        var valueValue = proc_value(value);
        return WhereData("${this.fullName} ${op} :${this.paramVarKeyName}", JsonMap(this.paramVarKeyName to valueValue))
    }


    /**
     * like 操作
     * @param value: 可以包含合法的 %,_
     */
    infix fun like(value: String): WhereData = this.column_match_value("like", value)

    /**
     * like "%查询内容%"
     */
    infix fun like_all(value: String): WhereData = this.column_match_value("like", ("%" + value + "%"))


    /**
     * 相等操作, 也可以比较 Json 数组的相等。
     */
    infix fun match(value: Serializable): WhereData {
        if (value is SqlColumnName) {
            return WhereData("${this.fullName} = ${value.fullName}")
        }

        //仅支持 数组相等。
        if (this.dbType == DbType.Json) {
            val v_type = value::class.java
            if (v_type.isArray) {
                var ary = value as Array<Any?>;
                return WhereData("${this.json_length()} = ${ary.size} and ${this.json_contains(db.sql.json_array(ary.toList()))} = 1")
            } else if (v_type.IsCollectionType) {
                var ary = value as Collection<Any?>;
                return WhereData("${this.json_length()} = ${ary.size} and ${this.json_contains(db.sql.json_array(ary))} = 1")
            } else {
                return WhereData("${this.json_length()} = 1 or ${this.json_contains(db.sql.json_array(listOf(value)))} = 1")
            }
        }
        return this.column_match_value("=", value);
    }

    infix fun match_regexp(value: String): WhereData {
        return this.column_match_value("regexp", value);
    }

    /*
    * 不等操作
    */
    infix fun match_not_equal(value: Serializable): WhereData {
        if (value is SqlColumnName) {
            return WhereData("${this.fullName} != ${value.fullName}")
        }

        //仅支持 数组相等。
        if (this.dbType == DbType.Json) {
            val v_type = value::class.java
            if (v_type.isArray) {
                var ary = value as Array<Any?>;
                return WhereData("(${this.json_length()} != ${ary.size} or ${this.json_overlaps(db.sql.json_array(ary.toList()))} = 0)")
            } else if (v_type.IsCollectionType) {
                var ary = value as Collection<Any?>;
                return WhereData("${this.json_length()} != ${ary.size} or ${this.json_overlaps(db.sql.json_array(ary))} = 0")
            } else {
                return WhereData("${this.json_length()} != 1 or ${this.json_overlaps(db.sql.json_array(listOf(value)))} = 0")
            }
        }
        return this.column_match_value("!=", value);
    }

    infix fun match_json_contains(value: Serializable): WhereData {
        if (value is SqlColumnName) {
            return WhereData("${this.json_contains(value)} = 1")
        }

        //仅支持 数组相等。
        if (this.dbType == DbType.Json) {
            val v_type = value::class.java
            if (v_type.isArray) {
                var ary = value as Array<Any?>;
                return WhereData("${this.json_contains(db.sql.json_array(ary.toList()))} = 1")
            } else if (v_type.IsCollectionType) {
                var ary = value as Collection<Any?>;
                return WhereData("${this.json_contains(db.sql.json_array(ary))} = 1")
            } else {
                return WhereData("${this.json_contains(db.sql.json_array(listOf(value)))} = 1")
            }
        }

        throw java.lang.RuntimeException("json_contains要求列必须是JSON类型！")
    }

    infix fun match_json_not_contains(value: Serializable): WhereData {
        if (value is SqlColumnName) {
            return WhereData("${this.json_contains(value)} = 0")
        }

        //仅支持 数组相等。
        if (this.dbType == DbType.Json) {
            val v_type = value::class.java
            if (v_type.isArray) {
                var ary = value as Array<Any?>;
                return WhereData("${this.json_contains(db.sql.json_array(ary.toList()))} = 0")
            } else if (v_type.IsCollectionType) {
                var ary = value as Collection<Any?>;
                return WhereData("${this.json_contains(db.sql.json_array(ary))} = 0")
            } else {
                return WhereData("${this.json_contains(db.sql.json_array(listOf(value)))} = 0")
            }
        }

        throw java.lang.RuntimeException("json_contains要求列必须是JSON类型！")
    }

    /**
     * 大于等于操作
     */
    infix fun match_gte(value: Serializable): WhereData {
        if (value is SqlColumnName) {
            return WhereData("${this.fullName} >= ${value.fullName}")
        }
        return this.column_match_value(">=", value)
    }

    /**
     * 大于操作，不包含等于
     */
    infix fun match_greaterThan(value: Serializable): WhereData {
        if (value is SqlColumnName) {
            return WhereData("${this.fullName} > ${value.fullName}")
        }
        return this.column_match_value(">", value);
    }

    /**
     * 小于等于操作。
     */
    infix fun match_lte(value: Serializable): WhereData {
        if (value is SqlColumnName) {
            return WhereData("${this.fullName} <= ${value.fullName}")
        }

        return this.column_match_value("<=", value)
    }

    /**
     * 小于操作，不包含等于
     */
    infix fun match_lessThan(value: Serializable): WhereData {
        if (value is SqlColumnName) {
            return WhereData("${this.fullName} < ${value.fullName}")
        }
        return this.column_match_value("<", value)
    }


    private fun column_match_between(min: Any, max: Any): WhereData {
        var minValue = proc_value(min);
        var maxValue = proc_value(max);

        return WhereData(
            "${this.fullName} >= :${this.paramVarKeyName}_min and ${this.fullName} < :${this.paramVarKeyName}_max",
            JsonMap("${this.paramVarKeyName}_min" to minValue, "${this.paramVarKeyName}_max" to maxValue)
        );
    }

    /**
     * 大于等于，并且 小于
     */
    fun <T : Serializable> match_between(min: T, max: T): WhereData {
        if (min is SqlColumnName && max is SqlColumnName) {
            return WhereData("${this.fullName} >= ${min.fullName} and ${this.fullName} < ${max.fullName}")
        }
        return this.column_match_between(min, max);
    }


    /**
     * in (values)操作
     */
    infix inline fun <reified T : Serializable> match_in(values: Array<T>): WhereData {
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
    infix fun SqlColumnName.match_in(select: SqlQueryClip<*, *>): WhereData {
        var subSelect = select.toSql()
        var ret = WhereData("${this.fullName} in ( ${subSelect.expression} )")
        ret.values += subSelect.values
        return ret;
    }

    /**
     * not in (values) 操作
     */
    infix inline fun <reified T : Serializable> match_not_in(values: Array<T>): WhereData {
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
    infix fun match_not_in(select: SqlQueryClip<*, *>): WhereData {
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
        } else if (this.dbType != DbType.Other) {
            emptyValue = " or ${this.fullName} = ''";
        }

        return WhereData("(${this.fullName} is null ${emptyValue})")
    }


    companion object {
        @JvmStatic
        fun of(name: String): SqlColumnName {
            return SqlColumnName(DbType.Other, "", name)
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
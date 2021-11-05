@file:JvmName("MyOqlSql")
@file:JvmMultifileClass

package nbcp.db.sql

import nbcp.comm.*
import nbcp.db.db
import java.io.Serializable
import java.lang.RuntimeException
import java.sql.PreparedStatement


fun SingleSqlData.toWhereData(): WhereData {
    return WhereData(this.expression, this.values);
}

//fun SingleSqlData.toColumnsData(): ColumnsData {
//    return ColumnsData(this.expression, this.values)
//}

fun SqlColumnNames.toSelectSql(): String =
    this.map {
        if (it.getAliasName() == it.name) it.fullName
        else
            it.fullName + " as " + db.sql.getSqlQuoteName(it.getAliasName())
    }.joinToString(",")


infix fun SqlColumnName.and(next: SqlColumnName): SqlColumnNames {
    return SqlColumnNames(this, next)
}

infix fun SqlColumnNames.and(next: SqlColumnName): SqlColumnNames {
    this.add(next)
    return this;
}

//val SqlColumnName.asc: SqlOrderBy
//    get() = SqlOrderBy(true, SingleSqlData(this.fullName));// this.toSingleSqlData())
//
//val SqlColumnName.desc: SqlOrderBy
//    get() = SqlOrderBy(false, SingleSqlData(this.fullName))

private fun op_ext_sql(sqlData: SingleSqlData, op: String, alias: String): SingleSqlData {
    var clone = sqlData.CloneObject()

    var alias_value = alias;
    if (alias_value.isEmpty()) {
        if (clone is SqlColumnName) {
            alias_value = "${op}_${clone.name}";
        } else {
            alias_value = "${op}_value"
        }
    }
    clone.expression = "${op}(${clone.expression}) ${alias_value}"
    return clone;
}

fun SingleSqlData.sum(alias: String): SingleSqlData {
    return op_ext_sql(this, "sum", alias);
}

fun SqlColumnName.count(alias: String): SingleSqlData {
    return op_ext_sql(this, "count", alias);
}

@JvmOverloads
fun SqlColumnName.min(alias: String = ""): SingleSqlData {
    return op_ext_sql(this, "min", alias);
}

@JvmOverloads
fun SqlColumnName.max(alias: String = ""): SingleSqlData {
    return op_ext_sql(this, "max", alias);
}

@JvmOverloads
fun SqlColumnName.avg(alias: String = ""): SingleSqlData {
    return op_ext_sql(this, "avg", alias);
}

//fun SqlColumnName.ifNull(elseValue: SingleSqlData, alias: String = ""): SingleSqlData {
//    return SingleSqlData(
//        "ifNull(${this.fullName},${elseValue.expression}) as ${alias.AsString(this.getAliasName())}",
//        elseValue.values
//    )
//}

/**
 * 字符个数
 */
@JvmOverloads
fun SqlColumnName.character_length(alias: String = ""): SingleSqlData {
    return op_ext_sql(this, "character_length", alias);
}

@JvmOverloads
fun SingleSqlData.ifNull(elseValue: SingleSqlData, alias: String = ""): SingleSqlData {
    var ret = this.CloneObject();
    ret.expression = "ifNull(${this.expression},"

    ret += elseValue

    ret.expression += ") ${alias}"
    return ret;
}

data class CaseWhenData<M : SqlBaseMetaTable<out T>, T : Serializable>(var mainEntity: M) : Serializable {
    private val caseWhens = mutableListOf<Pair<WhereData, SingleSqlData>>()
    private lateinit var elseEnd: Pair<SingleSqlData, String>

    fun whenThen(caseWhen: (M) -> WhereData, then: SingleSqlData): CaseWhenData<M, T> {
        this.caseWhens.add(caseWhen(this.mainEntity) to then)
        return this;
    }

    fun elseEnd(elseEnd: SingleSqlData, alias: String): SingleSqlData {
        var ret = SingleSqlData();
        ret.expression += "case";

        this.caseWhens.forEach {
            var where = it.first.toSingleData()

            ret.expression += " when "
            ret += where

            ret.expression += " then "
            ret += it.second
        }

        ret.expression += " else "
        ret += elseEnd

        ret.expression += " end ${alias}"

        return ret;
    }
}


fun <M : SqlBaseMetaTable<out T>, T : Serializable> M.case(): CaseWhenData<M, T> {
    return CaseWhenData(this)
}


/**
 * @param index : 从1开始.
 */
//fun PreparedStatement.setValue(index: Int, param: SqlParameterData) {
//    var sqlType = DbType.of(param.type).sqlType
//    if (param.value == null) {
//        this.setNull(index, sqlType)
//        return
//    }
//
//    var value = param.value
//
//    if (sqlType == java.sql.Types.VARCHAR) {
//        this.setString(index, value.AsString())
//        return
//    } else if (sqlType == java.sql.Types.INTEGER) {
//        this.setInt(index, param.value.AsInt())
//        return
//    } else if (sqlType == java.sql.Types.BIGINT) {
//        this.setLong(index, param.value.AsLong())
//        return
//    } else if (sqlType == java.sql.Types.SMALLINT) {
//        this.setShort(index, param.value.AsInt().toShort())
//        return
//    } else if (sqlType == java.sql.Types.TINYINT) {
//        this.setByte(index, param.value.AsInt().toByte())
//        return
//    } else if (sqlType == java.sql.Types.BIT) {
//        this.setByte(index, param.value.AsInt().toByte())
//        return
//    } else if (sqlType == java.sql.Types.TIMESTAMP) {
//        var v = param.value.AsLocalDateTime()
//        if (v == null) {
//            this.setTimestamp(index, null);
//        } else {
//            this.setTimestamp(index, java.sql.Timestamp.valueOf(v))
//        }
//        return
//    } else if (sqlType == java.sql.Types.DATE) {
//        var v = param.value.AsDate()
//        if (v == null) {
//            this.setDate(index, null);
//        } else {
//            this.setDate(index, java.sql.Date(v.time))
//        }
//        return
//    } else if (sqlType == java.sql.Types.TIME) {
//        var v = param.value.AsLocalTime();
//        if (v == null) {
//            this.setTime(index, null);
//        } else {
//            this.setTime(index, java.sql.Time(v.toSecondOfDay() * 1000L))
//        }
//        return
//    } else if (sqlType == java.sql.Types.FLOAT) {
//        this.setFloat(index, param.value.AsFloat())
//        return
//    } else if (sqlType == java.sql.Types.DOUBLE) {
//        this.setDouble(index, param.value.AsDouble())
//        return
//    } else if (sqlType == java.sql.Types.DECIMAL) {
//        this.setBigDecimal(index, param.value.AsBigDecimal())
//        return
//    }
//
//    throw RuntimeException("不识别的数据类型:${index} , ${sqlType}")
//}


val SqlBaseMetaTable<out Serializable>.quoteTableName: String
    get() = "${db.sql.getSqlQuoteName(this.tableName)}"

/**
 * 如果有别名，返回： table as t
 * 否则返回   table
 */
val SqlBaseMetaTable<out Serializable>.fromTableName: String
    get() {
        var ret = "${db.sql.getSqlQuoteName(this.tableName)}"
        if (this.getAliaTableName().HasValue && (this.getAliaTableName() != this.tableName)) {
            ret += " as " + db.sql.getSqlQuoteName(this.getAliaTableName());
        }
        return ret;
    }

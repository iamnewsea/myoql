package nbcp.db.sql

import nbcp.comm.*
import nbcp.db.db
import java.io.Serializable
import java.lang.RuntimeException
import java.math.BigDecimal
import java.sql.Date
import java.sql.PreparedStatement
import java.time.*


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


fun SqlColumnName.sum(alias: String = ""): SingleSqlData {
    return SingleSqlData("sum(${this.fullName}) ${alias.AsString("sum_" + this.name)}")
}

fun SqlColumnName.count(alias: String = ""): SingleSqlData {
    return SingleSqlData("count(${this.fullName}) ${alias.AsString("count_" + this.name)}")
}

fun SqlColumnName.min(alias: String = ""): SingleSqlData {
    return SingleSqlData("min(${this.fullName}) ${alias.AsString("min_" + this.name)}")
}

fun SqlColumnName.max(alias: String = ""): SingleSqlData {
    return SingleSqlData("max(${this.fullName}) ${alias.AsString("max_" + this.name)}")
}

fun SqlColumnName.avg(alias: String = ""): SingleSqlData {
    return SingleSqlData("avg(${this.fullName}) ${alias.AsString("avg_" + this.name)}")
}

fun SqlColumnName.ifNull(elseValue: SingleSqlData, alias: String = ""): SingleSqlData {
    return SingleSqlData("ifNull(${this.fullName},${elseValue.expression}) as ${alias.AsString(this.getAliasName())}", elseValue.values)
}


fun SingleSqlData.ifNull(elseValue: SingleSqlData, alias: String): SingleSqlData {
    var ret = this.CloneObject();
    ret.expression = "ifNull(${this.expression},"

    ret += elseValue

    ret.expression += ") ${alias}"
    return ret;
}

data class CaseWhenData<M : SqlBaseTable<out T>, T : IBaseDbEntity>(var mainEntity: M) : Serializable {
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


fun <M : SqlBaseTable<out T>, T : IBaseDbEntity> M.case(): CaseWhenData<M, T> {
    return CaseWhenData(this)
}


/**
 * @param index : 从1开始.
 */
fun PreparedStatement.setValue(index: Int, param: SqlParameterData) {
    var sqlType = DbType.of(param.type).sqlType
    if (param.value == null) {
        this.setNull(index, sqlType)
        return
    }

    var value = param.value

    if (sqlType == java.sql.Types.VARCHAR) {
        this.setString(index, value.AsString())
        return
    } else if (sqlType == java.sql.Types.INTEGER) {
        this.setInt(index, param.value.AsInt())
        return
    } else if (sqlType == java.sql.Types.BIGINT) {
        this.setLong(index, param.value.AsLong())
        return
    } else if (sqlType == java.sql.Types.SMALLINT) {
        this.setShort(index, param.value.AsInt().toShort())
        return
    } else if (sqlType == java.sql.Types.TINYINT) {
        this.setByte(index, param.value.AsInt().toByte())
        return
    } else if (sqlType == java.sql.Types.BIT) {
        this.setByte(index, param.value.AsInt().toByte())
        return
    } else if (sqlType == java.sql.Types.TIMESTAMP) {
        this.setTimestamp(index, java.sql.Timestamp.valueOf(param.value.AsLocalDateTime()))
        return
    } else if (sqlType == java.sql.Types.DATE) {
        this.setDate(index, java.sql.Date(param.value.AsDate().time))
        return
    } else if (sqlType == java.sql.Types.TIME) {
        this.setTime(index, java.sql.Time(param.value.AsLocalTime().toSecondOfDay() * 1000L))
        return
    } else if (sqlType == java.sql.Types.FLOAT) {
        this.setFloat(index, param.value.AsFloat())
        return
    } else if (sqlType == java.sql.Types.DOUBLE) {
        this.setDouble(index, param.value.AsDouble())
        return
    } else if (sqlType == java.sql.Types.DECIMAL) {
        this.setBigDecimal(index, param.value.AsBigDecimal())
        return
    }

    throw RuntimeException("不识别的数据类型:${index} , ${sqlType}")
}


val SqlBaseTable<out IBaseDbEntity>.quoteTableName: String
    get() = "${db.sql.getSqlQuoteName(this.tableName)}"

/**
 * 如果有别名，返回： table as t
 * 否则返回   table
 */
val SqlBaseTable<out IBaseDbEntity>.fromTableName: String
    get() {
        var ret = "${db.sql.getSqlQuoteName(this.tableName)}"
        if (this.getAliaTableName().HasValue && (this.getAliaTableName() != this.tableName)) {
            ret += " as " + db.sql.getSqlQuoteName(this.getAliaTableName());
        }
        return ret;
    }

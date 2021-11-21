package nbcp.db.sql

import nbcp.comm.AsString
import nbcp.comm.HasValue


open class SqlColumnName(
        val dbType: DbType,
        tableName: String,
        name: String
) : AliasBaseSqlSect() {
    /**
     * 表名
     */
    var tableName: String = tableName

    /**
     * 列名
     */
    var name: String = name


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
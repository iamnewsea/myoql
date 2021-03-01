package nbcp.db.sql

import nbcp.comm.AsString
import nbcp.comm.HasValue
import nbcp.db.*
import nbcp.db.sql.*
import java.io.Serializable


open class SqlColumnName(
    val dbType: DbType,
    tableName: String,
    name: String
) : SingleSqlData() {
    var tableName: String = tableName
        get() {
            return field
        }
        set(value) {
            field = value;
            super.expression = this.fullName
        }

    var name: String = name
        get() {
            return field
        }
        set(value) {
            field = value
            super.expression = this.fullName
        }

    init {
        super.expression = this.fullName
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

    private var columnAliaValue: String = ""

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
    open val jsonKeyName: String
        get() {
            if (columnAliaValue.HasValue) return this.columnAliaValue

            if (this.tableName.HasValue) {
                return "${this.tableName}_${this.name}"
            }

            //按常数列, 函数列,表达式来对待
            return "${this.name}"
        }

    fun alias(alias: String): SqlColumnName {
        if (alias == this.name) {
            this.columnAliaValue = ""
            return this;
        }

        var ret = SqlColumnName(dbType, tableName, name);
        ret.columnAliaValue = alias;
        return ret;
    }

    /**
     * 返回 columnAliaValue.AsString( name )
     */
    fun getAliasName(): String = this.columnAliaValue.AsString(this.name)

    override fun toString(): String {
        return name
    }

    override fun equals(other: Any?): Boolean {
        //地址。
        if (super.equals(other)) return true;

        if (other == null) return false;
        if (other is SqlColumnName) {
            return this.dbType == other.dbType && this.tableName == other.tableName && this.name == other.name && this.columnAliaValue == other.columnAliaValue
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
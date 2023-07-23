package nbcp.db.sql

import java.io.Serializable


data class SqlOrderBy(var Asc: Boolean, var orderBy: SqlParameterData) :Serializable{
    fun toSingleSqlData(): SqlParameterData {
        if (orderBy.expression.isEmpty()) {
            return SqlParameterData()
        }
        return SqlParameterData(" ${this.orderBy.expression} ${if (this.Asc) "asc" else "desc"}", this.orderBy.values)
    }
}


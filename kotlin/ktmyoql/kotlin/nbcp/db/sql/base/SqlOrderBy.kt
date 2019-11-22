package nbcp.db.sql

import java.io.Serializable


data class SqlOrderBy(var Asc: Boolean, var orderBy: SingleSqlData) :Serializable{
    fun toSingleSqlData(): SingleSqlData {
        if (orderBy.expression.isEmpty()) {
            return SingleSqlData()
        }
        return SingleSqlData(" ${this.orderBy.expression} ${if (this.Asc) "asc" else "desc"}", this.orderBy.values)
    }
}



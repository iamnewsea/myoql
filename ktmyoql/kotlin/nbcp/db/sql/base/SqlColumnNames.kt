package nbcp.db.sql

import nbcp.base.extend.HasValue
import nbcp.db.sql.SqlColumnNames
import java.io.Serializable


class SqlColumnNames() : ArrayList<SqlColumnName>(), Serializable {
    constructor(vararg columns: SqlColumnName) : this() {
        this.addAll(columns)
    }

    fun containsColumn(name: String): Boolean {
        return this.any { it.name == name }
    }
}
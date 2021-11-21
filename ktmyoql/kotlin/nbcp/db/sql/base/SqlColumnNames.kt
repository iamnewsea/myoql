package nbcp.db.sql

import java.io.Serializable


class SqlColumnNames() : ArrayList<SqlColumnName>(), Serializable {
    constructor(vararg columns: SqlColumnName) : this() {
        this.addAll(columns)
    }

    fun containsColumn(name: String): Boolean {
        return this.any { it.name == name }
    }
}
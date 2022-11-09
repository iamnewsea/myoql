package nbcp.myoql.db.sql.base

import java.io.Serializable


class SqlColumnNames() : ArrayList<SqlColumnName>(), Serializable {
    constructor(vararg columns: SqlColumnName) : this() {
        this.addAll(columns)
    }

    fun containsColumn(name: String): Boolean {
        return this.any { it.name == name }
    }

    fun getColumn(name: String): SqlColumnName? {
        return this.firstOrNull { it.name == name }
    }
}
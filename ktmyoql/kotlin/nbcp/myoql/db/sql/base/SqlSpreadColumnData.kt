package nbcp.myoql.db.sql.base

data class SqlSpreadColumnData(var column: String, var split: String = "_"){
    override fun toString(): String {
        return this.getPrefixName()
    }

    fun getPrefixName():String{
        return this.column + this.split;
    }
}


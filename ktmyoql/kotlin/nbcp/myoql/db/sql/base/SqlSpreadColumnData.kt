package nbcp.myoql.db.sql.base

import java.lang.annotation.Inherited
import java.lang.reflect.Field
import kotlin.reflect.KClass

data class SqlSpreadColumnData(var column: String, var split: String = "_"){
    override fun toString(): String {
        return this.getPrefixName()
    }

    fun getPrefixName():String{
        return this.column + this.split;
    }
}


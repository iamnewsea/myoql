package nbcp.myoql.db.sql.base

import java.io.Serializable

abstract class BaseAliasSqlSect : Serializable {
    var aliaValue: String = ""

    abstract fun toSingleSqlData(): SqlParameterData;
    open fun getAliasName(): String {
        return this.aliaValue
    }
}
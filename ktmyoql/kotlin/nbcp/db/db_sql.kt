package nbcp.db

import nbcp.base.utils.SpringUtil
import nbcp.db.sql.SqlBaseTable
import nbcp.db.sql.SqlEntityEvent

object db_sql{

    var getSqlEntity: ((tableName: String) -> SqlBaseTable<*>)? = null


    val sqlEvents by lazy {
        return@lazy SpringUtil.getBean<SqlEntityEvent>();
    }

    fun getSqlQuoteName(value: String): String {
        if (db.currentDatabaseType == DatabaseEnum.Mysql) {
            return "`${value}`"
        } else if (db.currentDatabaseType == DatabaseEnum.Mssql) {
            return "[${value}]"
        } else {
            return """"${value}""""
        }
    }


}
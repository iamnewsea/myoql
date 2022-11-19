package nbcp.myoql.db.enums

import nbcp.base.extend.IsIn


enum class DatabaseEnum {
    MONGO,
    REDIS,
    HBASE,
    ELASTIC_SEARCH,

    SQL,
    MY_SQL,
    ORACLE,
    SQLITE,
    SQL_SERVER,
    POSTGRE_SQL;


    fun isSqlType(): Boolean {
        if (this.IsIn(SQL, MY_SQL, ORACLE, SQLITE, SQL_SERVER, POSTGRE_SQL)) {
            return true;
        }
        return false;
    }
}

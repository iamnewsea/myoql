package nbcp.myoql.db.enums

import nbcp.base.extend.IsIn


enum class DatabaseEnum {
    MONGO,
    REDIS,
    HBASE,
    ELASTIC_SEARCH,

    SQL,
    DB2,
    MY_SQL,
    MARIA_DB,
    ORACLE,
    SQLITE,
    SQL_SERVER,
    POSTGRESQL;


    fun isSqlType(): Boolean {
        if (this.IsIn(SQL, DB2,MY_SQL, MARIA_DB, ORACLE, SQLITE, SQL_SERVER, POSTGRESQL)) {
            return true;
        }
        return false;
    }
}

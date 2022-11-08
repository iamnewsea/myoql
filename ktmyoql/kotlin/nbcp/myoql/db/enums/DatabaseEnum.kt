package nbcp.myoql.db.enums

import nbcp.base.extend.IsIn


enum class DatabaseEnum {
    Mongo,
    Redis,
    Hbase,
    ElasticSearch,

    Sql,
    Mysql,
    Oracle,
    Sqlite,
    SqlServer,
    Postgre;


    fun isSqlType(): Boolean {
        if (this.IsIn(Sql, Mysql, Oracle, Sqlite, SqlServer, Postgre)) {
            return true;
        }
        return false;
    }
}

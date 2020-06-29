package nbcp.db

import nbcp.utils.*
import nbcp.comm.StringKeyMap
import nbcp.db.sql.IDataGroup
import nbcp.db.sql.SqlBaseMetaTable
import nbcp.db.sql.SqlEntityEvent
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource

/**
 * 请使用 db.sql
 */
object db_sql {

    //所有的组。
    val groups = mutableSetOf<IDataGroup>()

    var getSqlEntity: ((tableName: String) -> SqlBaseMetaTable<*>)? = null


    val sqlEvents by lazy {
        return@lazy SpringUtil.getBean<SqlEntityEvent>();
    }

    fun getSqlQuoteName(value: String): String {
        if (db.mainDatabaseType == DatabaseEnum.Mysql) {
            return "`${value}`"
        } else if (db.mainDatabaseType == DatabaseEnum.Mssql) {
            return "[${value}]"
        } else {
            return """"${value}""""
        }
    }


    private var dynamicTableDataSource = StringKeyMap<DataSource>();
    /**
     * 指派集合到数据库
     */
    fun bindTableName2Database(tableName: String, data: DataSource) {
        this.dynamicTableDataSource.set(tableName, data)
    }

    fun unbindTableName2Database(tableName: String) {
        this.dynamicTableDataSource.remove(tableName)
    }
    /**
     * 根据集合定义，获取 MongoTemplate
     */
    fun getJdbcTemplateByTableName(tableName: String): JdbcTemplate? {
        var dataSource = dynamicTableDataSource.get(tableName);
        if (dataSource == null) return null;

        return JdbcTemplate(dataSource, true)
    }

}
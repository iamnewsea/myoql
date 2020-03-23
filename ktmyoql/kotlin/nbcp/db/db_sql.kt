package nbcp.db

import nbcp.base.utils.SpringUtil
import nbcp.comm.StringMap
import nbcp.comm.StringTypedMap
import nbcp.db.sql.SqlBaseTable
import nbcp.db.sql.SqlEntityEvent
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource

/**
 * 请使用 db.sql
 */
object db_sql {

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


    private var dynamicTableDataSource = StringTypedMap<DataSource>();
    /**
     * 指派集合到数据库
     */
    fun assignTableName2Database(tableName: String, data: DataSource) {
        this.dynamicTableDataSource.set(tableName, data)
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
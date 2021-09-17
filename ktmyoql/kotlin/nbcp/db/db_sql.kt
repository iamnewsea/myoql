package nbcp.db

import com.zaxxer.hikari.HikariDataSource
import nbcp.comm.AsString
import nbcp.comm.ToEnum
import nbcp.comm.config
import nbcp.utils.*
import nbcp.db.sql.IDataGroup
import nbcp.db.sql.SqlBaseMetaTable
import nbcp.db.sql.event.*
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.util.StringUtils
import javax.sql.DataSource

/**
 * 请使用 db.sql
 */
object db_sql {

    //所有的组。
    val groups = mutableSetOf<IDataGroup>()

//    var getSqlEntity: ((tableName: String) -> SqlBaseMetaTable<*>)? = null


    val sqlEvents by lazy {
        return@lazy SpringUtil.getBean<SqlEntityCollector>();
    }

    @JvmStatic
    val sqlDatabaseType: DatabaseEnum? by lazy {
        if (SpringUtil.containsBean(DataSourceAutoConfiguration::class.java) == false) {
            return@lazy null;
        }

        var conn = config.getConfig("spring.datasource.url").AsString();

        if (conn.startsWith("jdbc:mysql://")) {
            return@lazy DatabaseEnum.Mysql
        }

        if (conn.startsWith("jdbc:sqlserver://")) {
            return@lazy DatabaseEnum.SqlServer
        }
        if (conn.startsWith("jdbc:oracle:")) {
            return@lazy DatabaseEnum.Oracle
        }
        if (conn.startsWith("jdbc:postgresql://")) {
            return@lazy DatabaseEnum.Postgre
        }
        return@lazy null;
    }

    fun getSqlQuoteName(value: String): String {
        if (sqlDatabaseType == DatabaseEnum.Mysql) {
            return "`${value}`"
        } else if (sqlDatabaseType == DatabaseEnum.SqlServer) {
            return "[${value}]"
        } else {
            return """"${value}""""
        }
    }


    private val dataSourceMap = mutableMapOf<String, DataSource>();

    fun getDataSource(uri: String, username: String, password: String): DataSource? {
        var key = "${uri}-${username}-${password}"
        var dataSource = dataSourceMap.get(key);
        if (dataSource != null) {
            return dataSource;
        }

        var properties = SpringUtil.getBean<DataSourceProperties>();

        properties.url = uri;
        properties.username = username;
        properties.password = password;

        dataSource =
            properties.initializeDataSourceBuilder().type(HikariDataSource::class.java).build() as HikariDataSource
        if (StringUtils.hasText(properties.name)) {
            dataSource.poolName = properties.name
        }

        dataSourceMap.put(key, dataSource);
        return dataSource
    }

//    /**
//     * 根据表名，以及是不是读取操作，动态返回DataSource
//     */
//    var sqlDataSourceFunc: ((String, Boolean) -> DataSource)? = null


//    private var dynamicTableDataSource = StringKeyMap<DataSource>();
//
//    /**
//     * 指派集合到数据库
//     */
//    fun bindTableName2Database(tableName: String, data: DataSource) {
//        this.dynamicTableDataSource.set(tableName, data)
//    }
//
//    fun unbindTableName2Database(tableName: String) {
//        this.dynamicTableDataSource.remove(tableName)
//    }
//
//    /**
//     * 根据集合定义，获取 JdbcTemplate
//     */
//    fun getJdbcTemplateByTableName(tableName: String): JdbcTemplate? {
//        var dataSource = dynamicTableDataSource.get(tableName);
//        if (dataSource == null) return null;
//
//        return JdbcTemplate(dataSource, true)
//    }

}
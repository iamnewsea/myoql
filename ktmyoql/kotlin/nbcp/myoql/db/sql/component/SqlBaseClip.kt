package nbcp.myoql.db.sql.component

import nbcp.base.extend.AsString
import nbcp.base.extend.HasValue
import nbcp.base.extend.scopes
import nbcp.base.utils.SpringUtil
import nbcp.myoql.db.db
import nbcp.myoql.db.sql.DataSourceScope
import nbcp.myoql.db.sql.SqlTableDataSource
import nbcp.myoql.db.sql.base.SqlParameterData
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.io.Serializable
import javax.sql.DataSource

/**
 * ORM解决80%的问题即可. 对于 自连接,复杂的查询, 直接写Sql吧.
 *
 * 1. 读写分离
 *
 *      当前线程中,如果对某一个表进行了修改,则之后对该表的查询,也定位到主库.
 *
 * https://www.jianshu.com/p/f728e8c131a9
 * 配置： spring.datasource 是主库的数据库连接。
 * 额外增加： spring.datasource-slave 表示是从库连接。参数继承 spring.datasource
 * 额外增加： spring.datasource-slave 表示是从库连接池。参数继承 spring.datasource.hikari
 * 这样的好处是，当没有配置 spring.datasource-slave，可当单库使用。
 *
 * 2. 切数据源
 *
 * usingScope(db.getJdbcTemplate("primary")){
 *
 * }
 *
 * 3. 事务使用方式：
transTemplate.execute(new TransactionCallback<Object>() {
@Override
public Object doInTransaction(TransactionStatus transactionStatus) {
// DML执行
jdbcTemplate.update("Delete from actor_new where actor_id=?", 11);

// 回滚
transactionStatus.setRollbackOnly();
return null;
}
});
 */
abstract class SqlBaseClip(var tableName: String) : Serializable {
    init {
        db.affectRowCount = -1
        db.lastAutoId = -1
    }

    companion object {
//        private val defaultJdbcTemplate by lazy {
//            return@lazy SpringUtil.getBean<JdbcTemplate>()
//        }

//        val jsonMapMapper = SpringUtil.getBean<JsonMapRowMapper>()

//        private val jdbcMap = linkedMapOf<String, JdbcTemplate>()

        // orm bean 代理 RequestCache 及 Redis Cache
//        @JvmStatic
//        val cacheService by lazy {
//            return@lazy SpringUtil.getBean<ProxyCache4SqlService>();
//        }


//        fun getJdbcTemplateByDatasrouce(datasourceName: String): JdbcTemplate {
//            if (datasourceName.HasValue) {
//                var ret = jdbcMap.get(datasourceName)
//                if (ret == null) {
//                    jdbcMap.set(datasourceName, JdbcTemplate(SpringUtil.getBeanByName<DataSource>(datasourceName)))
//                    ret = jdbcMap.get(datasourceName)
//                }
//
//                return ret!!
//            } else return defaultJdbcTemplate
//        }
    }

    /**
     * 动态数据源：
     * 1. 配置文件
     * 2. 继承了 IDataSource 的Bean
     * 3. 当前作用域
     * 4. 如果是读操作，则使用 slave , 否则使用默认
     */
    val jdbcTemplate: NamedParameterJdbcTemplate
        get() {
            var isRead = this is SqlBaseQueryClip;

            var config = SpringUtil.getBean<SqlTableDataSource>();
            var dataSourceName = config.getDataSourceName(this.tableName, isRead)

            if (dataSourceName.HasValue) {

                var uri = SpringUtil.context.environment.getProperty("app.sql.${dataSourceName}.ds.uri").AsString()
                var username =
                    SpringUtil.context.environment.getProperty("app.sql.${dataSourceName}.ds.username").AsString()
                var password =
                    SpringUtil.context.environment.getProperty("app.sql.${dataSourceName}.ds.password").AsString()

                //其它参数按数据源配置参数
                var ds = db.sql.getDataSource(uri, username, password);

                return NamedParameterJdbcTemplate(ds);
            }

            var ds =
                db.sql.sqlEvents?.getDataSource(this.tableName, isRead) ?: scopes.getLatest<DataSourceScope>()?.value
            if (ds != null) {
                return NamedParameterJdbcTemplate(ds);
            }

            if (isRead) {
                if (SpringUtil.containsBean("slave", DataSource::class.java)) {
                    ds = SpringUtil.getBeanByName<DataSource>("slave")
                    return NamedParameterJdbcTemplate(ds);
                }
            }

            ds = SpringUtil.getBean<DataSource>()
            return NamedParameterJdbcTemplate(ds);
        }


//    val transactionTemplate: TransactionTemplate
//        get() {
//            if (datasourceName.HasValue) {
//                var dataSource = SpringUtil.getBeanByName<DataSource>(datasourceName)
//                var tx = DataSourceTransactionManager(dataSource)
//                return TransactionTemplate(tx)
//            }
//
//            return TransactionTemplate(DataSourceTransactionManager(SpringUtil.getBean<DataSource>()))
//        }


    abstract fun toSql(): SqlParameterData
}






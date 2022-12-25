package nbcp.myoql.db.mybatis


import nbcp.base.utils.SpringUtil
import nbcp.myoql.db.db
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.TransactionManagementConfigurer
import javax.sql.DataSource


/**
 * 依赖配置 app.mybatis.package
 */


open class MyBatisTransactionManagementConfig() : TransactionManagementConfigurer {
    val dataSource: DataSource
    get() {
        return db.sql.getScopeDataSource()!!;
    }

    override fun annotationDrivenTransactionManager(): PlatformTransactionManager {
        return DataSourceTransactionManager(dataSource)
    }
}
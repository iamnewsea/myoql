package nbcp.myoql.db.mybatis


import nbcp.base.utils.*
import org.springframework.context.annotation.*
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.TransactionManagementConfigurer
import java.util.*
import javax.sql.DataSource


/**
 * 依赖配置 app.mybatis.package
 */


open class MyBatisTransactionManagementConfig() : TransactionManagementConfigurer {
    val dataSource: DataSource by lazy {
        return@lazy SpringUtil.getBean<DataSource>()
    }

    override fun annotationDrivenTransactionManager(): PlatformTransactionManager {
        return DataSourceTransactionManager(dataSource)
    }
}
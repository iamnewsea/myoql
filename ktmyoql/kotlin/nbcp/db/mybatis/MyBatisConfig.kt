package nbcp.db.mybatis

import nbcp.base.utils.MyUtil
import nbcp.base.utils.SpringUtil
import nbcp.db.mysql.MysqlConfig
import org.apache.ibatis.executor.Executor
import org.apache.ibatis.executor.keygen.SelectKeyGenerator
import org.apache.ibatis.mapping.MappedStatement
import org.apache.ibatis.mapping.SqlCommandType
import org.apache.ibatis.plugin.*
import org.apache.ibatis.session.ResultHandler
import org.apache.ibatis.session.RowBounds
import org.apache.ibatis.session.SqlSessionFactory
import org.apache.poi.util.StringUtil
import org.mybatis.spring.SqlSessionFactoryBean
import org.mybatis.spring.SqlSessionTemplate
import org.mybatis.spring.mapper.MapperScannerConfigurer
import org.mybatis.spring.transaction.SpringManagedTransaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.Lazy
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.annotation.TransactionManagementConfigurer
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.util.*
import javax.annotation.Resource
import javax.sql.DataSource
import kotlin.reflect.KClass


/**
 * 依赖配置 app.mybatis.package
 */
@Configuration
@EnableTransactionManagement
@AutoConfigureAfter(value = arrayOf(DataSourceAutoConfiguration::class))
@ConditionalOnProperty("app.mybatis.package")
@DependsOn(value = arrayOf("mysqlConfig", "primary"))
@ConditionalOnBean(value = arrayOf(MysqlConfig::class))
@Lazy
open class MyBatisConfig() : TransactionManagementConfigurer {
    val dataSource: DataSource by lazy {
        return@lazy SpringUtil.getBean<DataSource>()
    }

    override fun annotationDrivenTransactionManager(): PlatformTransactionManager {
        return DataSourceTransactionManager(dataSource)
    }

    @Bean
    @Lazy
    fun mapperScannerConfigurer(): MapperScannerConfigurer {
        val mapperScannerConfigurer = MapperScannerConfigurer()
        //获取之前注入的beanName为sqlSessionFactory的对象
        mapperScannerConfigurer.setSqlSessionFactoryBeanName("sqlSessionFactory")
        //指定xml配置文件的路径
        mapperScannerConfigurer.setBasePackage(SpringUtil.context.environment.getProperty("app.mybatis.package"))
        return mapperScannerConfigurer
    }

    @Bean(name = arrayOf("sqlSessionFactory"))
    @Lazy
    open fun sqlSessionFactoryBean(): SqlSessionFactory? {
        val bean = SqlSessionFactoryBean()
        bean.setDataSource(dataSource)

        var config = org.apache.ibatis.session.Configuration()
        config.isCacheEnabled = true

//        config.addCache(RedisCacheMyBatis())

//        config.addInterceptor(QueryInterceptor())
//        config.addInterceptor(UpdateInterceptor())
        config.addInterceptor(MyBatisInterceptor())

        bean.setConfiguration(config)
        try {
            return bean.`object`
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException(e)
        }

    }

    @Bean
    @Lazy
    open fun sqlSessionTemplate(sqlSessionFactory: SqlSessionFactory): SqlSessionTemplate {
        return SqlSessionTemplate(sqlSessionFactory)
    }
}
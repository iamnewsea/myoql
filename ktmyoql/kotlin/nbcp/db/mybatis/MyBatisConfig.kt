package nbcp.db.mybatis

import nbcp.base.utils.MyUtil
import nbcp.base.utils.SpringUtil
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.annotation.TransactionManagementConfigurer
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.util.*
import javax.annotation.Resource
import javax.sql.DataSource


/**
 * 依赖配置 app.mybatis.package
 */
@Configuration
@EnableTransactionManagement
@ConditionalOnProperty("app.mybatis.package")
@ConditionalOnClass(MapperScannerConfigurer::class)
open class MyBatisConfig : TransactionManagementConfigurer {
    lateinit var dataSource: DataSource

    override fun annotationDrivenTransactionManager(): PlatformTransactionManager {
        return DataSourceTransactionManager(dataSource)
    }

    @Bean
    fun mapperScannerConfigurer(): MapperScannerConfigurer {
        val mapperScannerConfigurer = MapperScannerConfigurer()
        //获取之前注入的beanName为sqlSessionFactory的对象
        mapperScannerConfigurer.setSqlSessionFactoryBeanName("sqlSessionFactory")
        //指定xml配置文件的路径
        mapperScannerConfigurer.setBasePackage(SpringUtil.context.environment.getProperty("app.mybatis.package"))
        return mapperScannerConfigurer
    }

    @Bean(name = arrayOf("sqlSessionFactory"))
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
    open fun sqlSessionTemplate(sqlSessionFactory: SqlSessionFactory): SqlSessionTemplate {
        return SqlSessionTemplate(sqlSessionFactory)
    }
}
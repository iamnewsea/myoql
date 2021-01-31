package nbcp.db.mybatis

import nbcp.comm.config
import nbcp.utils.*
import org.apache.ibatis.session.SqlSessionFactory
import org.mybatis.spring.SqlSessionFactoryBean
import org.mybatis.spring.SqlSessionTemplate
import org.mybatis.spring.mapper.MapperScannerConfigurer
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.annotation.*
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.annotation.TransactionManagementConfigurer
import java.util.*
import javax.sql.DataSource


/**
 * 依赖配置 app.mybatis.package
 */
@Configuration
@EnableTransactionManagement
@AutoConfigureAfter(value = [DataSourceAutoConfiguration::class])
@ConditionalOnProperty("app.mybatis.package")
//@DependsOn(value = arrayOf("mysqlConfig", "primary", "springUtil"))
@ConditionalOnBean(DataSourceAutoConfiguration::class)
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
        mapperScannerConfigurer.setBasePackage(config.mybatisPackage)
        return mapperScannerConfigurer
    }

    @Bean(name = ["sqlSessionFactory"])
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
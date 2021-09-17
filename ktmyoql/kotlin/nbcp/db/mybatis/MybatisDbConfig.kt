package nbcp.db.mybatis

import nbcp.comm.config
import nbcp.utils.SpringUtil
import org.apache.ibatis.session.SqlSessionFactory
import org.mybatis.spring.SqlSessionFactoryBean
import org.mybatis.spring.SqlSessionTemplate
import org.mybatis.spring.mapper.MapperScannerConfigurer
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.context.event.ApplicationPreparedEvent
import org.springframework.context.annotation.Import
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource

@EnableTransactionManagement
@Component
@Import(SpringUtil::class)
@ConditionalOnClass(SqlSessionFactory::class)
class MybatisDbConfig {

    @EventListener
    fun prepared(ev: ApplicationPreparedEvent) {
        if (SpringUtil.context.environment.getProperty("spring.datasource.url").isNullOrEmpty() &&
            SpringUtil.context.environment.getProperty("spring.datasource.hikari.jdbc-url").isNullOrEmpty()
        ) {
            return;
        }

        if (config.mybatisPackage.isEmpty()) {
            return;
        }

        if (SpringUtil.containsBean(DataSourceAutoConfiguration::class.java) == false) {
            return;
        }


        SpringUtil.registerBeanDefinition(mapperScannerConfigurer())
        SpringUtil.registerBeanDefinition(MyBatisTransactionManagementConfig())

        var sqlSessionFactoryBean = sqlSessionFactoryBean();
        if (sqlSessionFactoryBean != null) {
            SpringUtil.registerBeanDefinition(sqlSessionFactoryBean)
            SpringUtil.registerBeanDefinition(sqlSessionTemplate(sqlSessionFactoryBean))
        }
    }


    val dataSource: DataSource by lazy {
        return@lazy SpringUtil.getBean<DataSource>()
    }


    fun mapperScannerConfigurer(): MapperScannerConfigurer {
        val mapperScannerConfigurer = MapperScannerConfigurer()
        //获取之前注入的beanName为sqlSessionFactory的对象
        mapperScannerConfigurer.setSqlSessionFactoryBeanName("sqlSessionFactory")
        //指定xml配置文件的路径
        mapperScannerConfigurer.setBasePackage(config.mybatisPackage)
        return mapperScannerConfigurer
    }


    fun sqlSessionFactoryBean(): SqlSessionFactory? {
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

    fun sqlSessionTemplate(sqlSessionFactory: SqlSessionFactory): SqlSessionTemplate {
        return SqlSessionTemplate(sqlSessionFactory)
    }
}
//package nbcp.db.mysql
//
//import com.zaxxer.hikari.HikariDataSource
//import nbcp.comm.*
//import nbcp.utils.*
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.beans.factory.annotation.Qualifier
//import org.springframework.boot.autoconfigure.AutoConfigureAfter
//import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
//import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
//import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
//import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
//import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
//import org.springframework.boot.context.properties.ConfigurationProperties
//import org.springframework.boot.jdbc.DataSourceBuilder
//import org.springframework.context.annotation.*
//import org.springframework.core.env.Environment
//import org.springframework.core.type.AnnotatedTypeMetadata
//import org.springframework.stereotype.Component
//import org.springframework.util.StringUtils
//import javax.sql.DataSource
//
///**
// * 效果： 主数据源与单数据源配置是一样的。  salve 内容可以 copy
// * 配置结构：
// * datasource:
// *    url: ...
// *    hikari:
// *        maximum-pool-size: 70
// * datasource-slave:
// *    url: ...
// *    hikari:
// *        maximum-pool-size: 70
// */
//@Configuration(proxyBeanMethods = false)
//@ConditionalOnProperty("spring.datasource.url")
//@Import(SpringUtil::class)
//@AutoConfigureAfter(DataSourceAutoConfiguration::class)
//@ConditionalOnBean(DataSourceAutoConfiguration::class)
//internal class MyOqlMySqlConfig {
//    companion object {
//        @JvmStatic
//        val hasSlave: Boolean
//            get() {
//                return SpringUtil.containsBean("slave", DataSource::class.java);
//            }
//    }
//
//    @Primary
//    @Bean()
//    @ConfigurationProperties("spring.datasource")
//    fun dataSourceProperties(): DataSourceProperties {
//        return DataSourceProperties()
//    }
//
//    @Primary
//    @Bean
//    @ConfigurationProperties(prefix = "spring.datasource.hikari")
//    fun myoqlDataSource(properties: DataSourceProperties): HikariDataSource {
//        val dataSource =
//            properties.initializeDataSourceBuilder().type(HikariDataSource::class.java).build() as HikariDataSource
//        if (StringUtils.hasText(properties.name)) {
//            dataSource.poolName = properties.name
//        }
//        return dataSource
//    }
//
//
//    @Bean("slaveDataSourceProperties")
//    @ConfigurationProperties("spring.datasource-slave")
//    fun slaveDataSourceProperties(): DataSourceProperties {
//        return DataSourceProperties()
//    }
//
//    @Bean("slave")
//    @ConfigurationProperties(prefix = "spring.datasource-slave.hikari")
//    fun slave(@Autowired @Qualifier("slaveDataSourceProperties") properties: DataSourceProperties): HikariDataSource {
//        val dataSource =
//            properties.initializeDataSourceBuilder().type(HikariDataSource::class.java).build() as HikariDataSource
//        if (StringUtils.hasText(properties.name)) {
//            dataSource.poolName = properties.name
//        }
//        return dataSource
//    }
//}
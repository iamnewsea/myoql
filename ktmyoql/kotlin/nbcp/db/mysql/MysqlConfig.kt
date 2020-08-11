package nbcp.db.mysql

import com.zaxxer.hikari.HikariDataSource
import nbcp.comm.AsString
import nbcp.utils.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.*
import org.springframework.core.env.Environment
import org.springframework.core.type.AnnotatedTypeMetadata
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource

class ExistsSlaveDataSourceConfigCondition : Condition {
    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata?): Boolean {
        return getValue(context.environment);
    }

    fun getValue(environment: Environment):Boolean {
        return (
                 environment.getProperty("spring.datasource.url") != null ||
                         environment.getProperty("spring.datasource.hikari.url") != null ||
                         environment.getProperty("spring.datasource.hikari.jdbc-url") != null
                )
                &&
                (
                     environment.getProperty("spring.datasource.slave.url") != null ||
                             environment.getProperty("spring.datasource.slave.hikari.url") != null ||
                             environment.getProperty("spring.datasource.slave.hikari.jdbc-url") != null
                )
    }
}

class ExistsDataSourceConfigCondition : Condition {
    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        return getValue(context.environment)
    }

    fun getValue(environment: Environment):Boolean {
        return environment.getProperty("spring.datasource.url") != null ||
                environment.getProperty("spring.datasource.hikari.url") != null ||
                environment.getProperty("spring.datasource.hikari.jdbc-url") != null
    }
}

/**
 * Mysql 连接配置，依赖配置 spring.datasource.url，默认驱动：com.mysql.cj.jdbc.Driver
 * 主配置
 * spring.datasource.hikari
 *      jdbcUrl,如果不存在取 spring.datasource.hikari.url,spring.datasource.url
 *      driverClassName,如果不存在取 spring.datasource.driver-class-name
 *      username,如果不存在取 spring.datasource.username
 *      password,如果不存在取 spring.datasource.password
 *
 * 从配置：
 * spring.datasource.slave.hikari
 *      jdbcUrl,如果不存在取 spring.datasource.slave.hikari.url,spring.datasource.slave.url
 *      driverClassName,如果不存在取 spring.datasource.slave.driverClassName
 *      username,如果不存在取 spring.datasource.slave.username
 *      password,如果不存在取 spring.datasource.slave.password
 */
@Configuration()
@AutoConfigureAfter(value = arrayOf(DataSourceAutoConfiguration::class))
@Conditional(ExistsDataSourceConfigCondition::class)
@DependsOn(value = arrayOf("springUtil"))
class MysqlConfig() {
    companion object {
        @JvmStatic
        val hasSlave by lazy {
            return@lazy SpringUtil.context.containsBean("slave");
        }
    }

    @Bean("primary")
    @Primary
    @ConfigurationProperties("spring.datasource.hikari")
    fun primaryDataSource(): DataSource {
        var ret = DataSourceBuilder.create().build() as HikariDataSource

        if (ret.jdbcUrl.isNullOrEmpty()) {
            ret.jdbcUrl = SpringUtil.context.environment.getProperty("spring.datasource.hikari.url")

            if (ret.jdbcUrl.isNullOrEmpty()) {
                ret.jdbcUrl = SpringUtil.context.environment.getProperty("spring.datasource.url")
            }
        }

        if (ret.driverClassName.isNullOrEmpty()) {
            ret.driverClassName = SpringUtil.context.environment.getProperty("spring.datasource.driver-class-name").AsString("com.mysql.cj.jdbc.Driver")
        }

        if (ret.username == null) {
            ret.username = SpringUtil.context.environment.getProperty("spring.datasource.username")
        }

        if (ret.password == null) {
            ret.password = SpringUtil.context.environment.getProperty("spring.datasource.password")
        }

        return ret;
    }


    @Bean("slave")
    @ConfigurationProperties("spring.datasource.slave.hikari")
    @Conditional(ExistsSlaveDataSourceConfigCondition::class)
//    @ConditionalOnExpression("\${spring.datasource.slave}")
    fun slaveDataSource(): DataSource {
        var ret = DataSourceBuilder.create().build() as HikariDataSource


        if (ret.jdbcUrl.isNullOrEmpty()) {
            ret.jdbcUrl = SpringUtil.context.environment.getProperty("spring.datasource.slave.hikari.url")
            if (ret.jdbcUrl.isNullOrEmpty()) {
                ret.jdbcUrl = SpringUtil.context.environment.getProperty("spring.datasource.slave.url")
            }
        }

        if (ret.driverClassName.isNullOrEmpty()) {
            ret.driverClassName = SpringUtil.context.environment.getProperty("spring.datasource.slave.driver-class-name").AsString("com.mysql.cj.jdbc.Driver")
        }



        if (ret.username == null) {
            ret.username = SpringUtil.context.environment.getProperty("spring.datasource.slave.username")
        }

        if (ret.password == null) {
            ret.password = SpringUtil.context.environment.getProperty("spring.datasource.slave.password")
        }

        return ret;
    }


    @Bean()
    @Primary
    fun primaryJdbcTemplate(): JdbcTemplate {
        return JdbcTemplate(SpringUtil.getBean<DataSource>(), true)
    }

    @Bean("slaveJdbcTemplate")
    @Conditional(ExistsSlaveDataSourceConfigCondition::class)
    fun slaveJdbcTemplate(): JdbcTemplate {
        if (MysqlConfig.hasSlave) {
            return JdbcTemplate(SpringUtil.getBeanByName<DataSource>("slave"), true)
        }

        return primaryJdbcTemplate();
    }
}
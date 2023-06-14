package nbcp.myoql.db.mysql

import com.zaxxer.hikari.HikariDataSource
import nbcp.base.comm.config
import nbcp.base.extend.HasValue
import nbcp.base.utils.ClassUtil
import nbcp.base.utils.SpringUtil
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.event.ApplicationPreparedEvent
import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.context.annotation.Conditional
import org.springframework.context.event.EventListener
import org.springframework.core.type.AnnotatedTypeMetadata
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import javax.sql.DataSource

@Component
//@Import(JsonMapRowMapper::class)
@Conditional(ExistsSqlSourceConfigCondition::class)
//@ConditionalOnProperty("spring.datasource.url")
class MySqlDataSourceConfig {
    companion object {
        @JvmStatic
        val hasSlave: Boolean
            get() {
                return SpringUtil.containsBean("slaveDataSource", DataSource::class.java);
            }
    }


    @EventListener
    fun prepared(ev: ApplicationPreparedEvent) {
        if (SpringUtil.context.environment.getProperty("spring.datasource.url").isNullOrEmpty() &&
            SpringUtil.context.environment.getProperty("spring.datasource.hikari.jdbc-url").isNullOrEmpty()
        ) {
            return;
        }
        if (SpringUtil.containsBean(DataSourceAutoConfiguration::class.java) == false) {
            return;
        }

        var jdbcs = SpringUtil.beanFactory.getBeansOfType(JdbcTemplate::class.java)
        if (jdbcs.size > 1 && !jdbcs.any { SpringUtil.beanFactory.getBeanDefinition(it.key).isPrimary }) {
            SpringUtil.beanFactory.getBeanDefinition("jdbcTemplate")?.isPrimary = true;
        }

        var dss = SpringUtil.beanFactory.getBeansOfType(DataSource::class.java)
        if (dss.size > 1 && !dss.any { SpringUtil.beanFactory.getBeanDefinition(it.key).isPrimary }) {
            SpringUtil.beanFactory.getBeanDefinition("dataSource")?.isPrimary = true;
        }


        var slaveDataProperties =
            SpringUtil.binder.bindOrCreate("spring.datasource-slave", DataSourceProperties::class.java);
        if (slaveDataProperties.url.HasValue) {
            var dataSourceSlave = slaveDataProperties.getDataSource()
            SpringUtil.registerBeanDefinition("slaveDataSource", dataSourceSlave)
            SpringUtil.registerBeanDefinition("slaveJdbcTemplate", NamedParameterJdbcTemplate(dataSourceSlave))
        }
    }


    private fun DataSourceProperties.getDataSource(): HikariDataSource {
        return this.initializeDataSourceBuilder().type(HikariDataSource::class.java)
            .build() as HikariDataSource
    }
}


class ExistsSqlSourceConfigCondition : Condition {
    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        if( config.getConfig("spring.datasource.url").isNullOrEmpty()){
            return false;
        }

        return ClassUtil.existsClass("org.mariadb.jdbc.Driver") ||
                ClassUtil.existsClass("com.mysql.cj.jdbc.Driver") ||
                ClassUtil.existsClass("com.microsoft.sqlserver.jdbc.SQLServerDriver") ||
                ClassUtil.existsClass("org.sqlite.JDBC") ||
                ClassUtil.existsClass("org.postgresql.Driver") ||
                ClassUtil.existsClass("oracle.jdbc.driver.OracleDriver")
    }
}


package nbcp.db.mysql

import com.mysql.cj.jdbc.MysqlDataSource
import com.zaxxer.hikari.HikariDataSource
import nbcp.comm.HasValue
import nbcp.db.sql.component.JsonMapRowMapper
import nbcp.utils.SpringUtil
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.event.ApplicationPreparedEvent
import org.springframework.context.annotation.Import
import org.springframework.context.event.EventListener
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import javax.sql.DataSource

@Component
@Import(JsonMapRowMapper::class)
@ConditionalOnClass(MysqlDataSource::class)
class MySqlDataSourceConfig {
    companion object {
        @JvmStatic
        val hasSlave: Boolean
            get() {
                return SpringUtil.containsBean("slave", DataSource::class.java);
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


        SpringUtil.beanFactory.getBeanDefinition("dataSource").isPrimary = true;
        SpringUtil.beanFactory.getBeanDefinition("jdbcTemplate").isPrimary = true;


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
package nbcp.db.mysql

import com.zaxxer.hikari.HikariDataSource
import nbcp.utils.SpringUtil
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.event.ApplicationPreparedEvent
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.Import
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import javax.sql.DataSource

@Component
@Import(SpringUtil::class)
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


        var dataSource = SpringUtil.binder.bindOrCreate("spring.datasource", DataSourceProperties::class.java).getDataSource()
        SpringUtil.registerBeanDefinition("dataSource", dataSource)

        var dataSourceSlave = SpringUtil.binder.bindOrCreate("spring.datasource-slave", DataSourceProperties::class.java)
        SpringUtil.registerBeanDefinition("slave", dataSourceSlave)
    }


    private fun DataSourceProperties.getDataSource(): HikariDataSource {
        return this.initializeDataSourceBuilder().type(HikariDataSource::class.java)
            .build() as HikariDataSource
    }
}
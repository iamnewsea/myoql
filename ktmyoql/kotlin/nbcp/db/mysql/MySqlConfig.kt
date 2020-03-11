package nbcp.db.mysql

import com.zaxxer.hikari.HikariDataSource
import nbcp.base.extend.AsString
import nbcp.base.utils.SpringUtil
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.*
import org.springframework.core.type.AnnotatedTypeMetadata
import javax.sql.DataSource

class ExistsSlaveDataSourceConfigCondition : Condition {
    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        return context.environment.getProperty("spring.datasource.slave") != null
    }
}

@Configuration
@AutoConfigureAfter(DataSourceAutoConfiguration::class)
//@ConditionalOnBean(DataSource::class)
class MySqlConfig {
    @Bean()
    @Primary
    @ConfigurationProperties("spring.datasource.hikari")
    fun primaryDataSource(): DataSource? {
        var ret = DataSourceBuilder.create().build() as HikariDataSource?
        if (ret == null) {
            return null;
        }

        if (ret.jdbcUrl == null) {
            ret.jdbcUrl = SpringUtil.context.environment.getProperty("spring.datasource.url")
        }
        if( ret.jdbcUrl == null){
            return null;
        }

        if (ret.driverClassName == null) {
            ret.driverClassName = SpringUtil.context.environment.getProperty("spring.datasource.driverClassName").AsString("com.mysql.cj.jdbc.Driver")
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
//    @Conditional(ExistsSlaveDataSourceConfigCondition::class)
//    @ConditionalOnExpression("\${spring.datasource.slave}")
    fun slaveDataSource(): DataSource? {
        var ret = DataSourceBuilder.create().build() as HikariDataSource?

        if (ret == null) {
            return null;
        }

        if (ret.jdbcUrl == null) {
            ret.jdbcUrl = SpringUtil.context.environment.getProperty("spring.datasource.slave.url")
        }

        if( ret.jdbcUrl == null){
            return null;
        }

        if (ret.driverClassName == null) {
            ret.driverClassName = SpringUtil.context.environment.getProperty("spring.datasource.slave.driverClassName").AsString("com.mysql.cj.jdbc.Driver")
        }



        if (ret.username == null) {
            ret.username = SpringUtil.context.environment.getProperty("spring.datasource.slave.username")
        }

        if (ret.password == null) {
            ret.password = SpringUtil.context.environment.getProperty("spring.datasource.slave.password")
        }

        return ret;
    }
}
package nbcp.db.sql

import nbcp.db.AbstractMultipleDataSourceProperties
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * 定义Sql不同的数据源
 */
@ConfigurationProperties(prefix = "app.sql")
@Component
class SqlTableDataSource : AbstractMultipleDataSourceProperties() {
}
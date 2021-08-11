package nbcp.db.sql

import nbcp.db.MultipleDataSourceProperties
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * 定义Mongo不同的数据源
 */
@ConfigurationProperties(prefix = "app.sql")
@Component
class SqlTableDataSource : MultipleDataSourceProperties() {
}
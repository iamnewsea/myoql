package nbcp.db

import com.mysql.cj.jdbc.MysqlDataSource
import nbcp.db.mybatis.DefaultPointcutAdvisor
import nbcp.db.mybatis.MybatisDbConfig
import nbcp.db.mysql.MySqlDataSourceConfig
import nbcp.db.mysql.service.UploadFileSqlService
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Component
import javax.sql.DataSource

@Component
@Import(
    value = [MySqlDataSourceConfig::class,
        MybatisDbConfig::class,
        DefaultPointcutAdvisor::class,
        UploadFileSqlService::class]
)
class MyOqlApplicationPreparedEvent {
}
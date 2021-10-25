package nbcp.db

import nbcp.db.mybatis.MyBatisRedisCachePointcutAdvisor
import nbcp.db.mybatis.MybatisDbConfig
import nbcp.db.mysql.MySqlDataSourceConfig
import nbcp.db.mysql.service.UploadFileSqlService
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Component

@Component
@Import(
    value = [MySqlDataSourceConfig::class,
        MybatisDbConfig::class,
        MyBatisRedisCachePointcutAdvisor::class,
        UploadFileSqlService::class]
)
class MyOqlApplicationPreparedEvent {
}
package nbcp

import nbcp.db.cache.RedisCacheAopService
import nbcp.db.es.EsEntityCollector
import nbcp.db.es.event.EsInsertEvent
import nbcp.db.es.tool.EsIndexDataSource
import nbcp.db.es.tool.EsTableLogProperties
import nbcp.db.mongo.MongoCollectionDataSource
import nbcp.db.mongo.MongoEntityCollector
import nbcp.db.mongo.event.*
import nbcp.db.mongo.service.UploadFileMongoService
import nbcp.db.mybatis.MyBatisRedisCachePointcutAdvisor
import nbcp.db.mybatis.MybatisDbConfig
import nbcp.db.mysql.MySqlDataSourceConfig
import nbcp.db.mysql.service.UploadFileSqlService
import nbcp.myOqlBeanProcessor.MyOqlJsonConfig
import nbcp.myOqlBeanProcessor.MyOqlMongoConfig
import nbcp.myOqlBeanProcessor.MyOqlRabbitMqConfig
import nbcp.myOqlBeanProcessor.MyOqlRedisConfig
import org.springframework.boot.context.event.ApplicationPreparedEvent
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Configuration
@Import(
    value = [
        EsEntityCollector::class,

        MongoEntityCollector::class,

        RedisCacheAopService::class,


        MyOqlJsonConfig::class,
        MyOqlMongoConfig::class,
        MyOqlRabbitMqConfig::class,
        MyOqlRedisConfig::class,

        //下面是数据库配置
        MySqlDataSourceConfig::class,
        MybatisDbConfig::class,
        MyBatisRedisCachePointcutAdvisor::class,
        UploadFileSqlService::class,
        UploadFileMongoService::class
    ]
)
//@ComponentScan("nbcp.db.mongo.event")
class MyOqlInitConfig {

    @EventListener
    fun prepared(ev: ApplicationPreparedEvent) {

    }
}
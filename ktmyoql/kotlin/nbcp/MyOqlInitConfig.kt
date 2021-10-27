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
import nbcp.bean.MyOqlJsonConfig
import nbcp.bean.MyOqlMongoConfig
import nbcp.bean.MyOqlRabbitMqConfig
import nbcp.bean.MyOqlRedisConfig
import nbcp.utils.ClassUtil
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.BeanFactoryAware
import org.springframework.boot.context.event.ApplicationPreparedEvent
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DeferredImportSelector
import org.springframework.context.annotation.Import
import org.springframework.context.event.EventListener
import org.springframework.core.type.AnnotationMetadata
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
class MyOqlInitConfig : DeferredImportSelector, BeanFactoryAware {

    @EventListener
    fun prepared(ev: ApplicationPreparedEvent) {

    }

    override fun selectImports(importingClassMetadata: AnnotationMetadata): Array<String> {

        var ret = mutableListOf<String>();
//        ClassUtil.getClassesWithAnnotationType("nbcp", Component::class.java)
//            .sortedWith({ o1, o2 ->
//
//                if (BeanDefinitionRegistryPostProcessor::class.java.isAssignableFrom(o1)) {
//                    return@sortedWith 1;
//                } else if (BeanDefinitionRegistryPostProcessor::class.java.isAssignableFrom(o2)) {
//                    return@sortedWith -1;
//                }
//
//                if (BeanFactoryPostProcessor::class.java.isAssignableFrom(o1)) {
//                    return@sortedWith 1;
//                } else if (BeanFactoryPostProcessor::class.java.isAssignableFrom(o2)) {
//                    return@sortedWith -1;
//                }
//
//                if (BeanPostProcessor::class.java.isAssignableFrom(o1)) {
//                    return@sortedWith 1;
//                } else if (BeanPostProcessor::class.java.isAssignableFrom(o2)) {
//                    return@sortedWith -1;
//                }
//
//                return@sortedWith 0;
//            })
//            .map { it.name }
//            .toTypedArray();

        return ret.toTypedArray();
    }

    private lateinit var beanFactory: BeanFactory;
    override fun setBeanFactory(beanFactory: BeanFactory) {
        this.beanFactory = beanFactory;
    }
}
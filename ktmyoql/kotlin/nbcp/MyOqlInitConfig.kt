package nbcp

import nbcp.bean.*
import nbcp.comm.Important
import nbcp.component.BaseJsonMapper
import nbcp.component.DbJsonMapper
import nbcp.db.mongo.event.*
import nbcp.db.mybatis.MyBatisRedisCachePointcutAdvisor
import nbcp.db.mybatis.MybatisDbConfig
import nbcp.db.mysql.MySqlDataSourceConfig
import nbcp.db.MyOqlBaseActionLogDefine
import nbcp.db.MyOqlMultipleDataSourceDefine
import nbcp.db.cache.RedisCacheAopService
import nbcp.db.db
import nbcp.db.es.*
import nbcp.db.mongo.*
import nbcp.db.sql.*
import nbcp.db.sql.event.*
import nbcp.model.IUploadFileDbService
import nbcp.utils.SpringUtil
import org.bson.Document
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.BeanFactoryAware
import org.springframework.beans.factory.config.BeanDefinitionHolder
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.boot.context.event.ApplicationPreparedEvent
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.ApplicationEvent
import org.springframework.context.ResourceLoaderAware
import org.springframework.context.annotation.*
import org.springframework.context.event.EventListener
import org.springframework.core.io.ResourceLoader
import org.springframework.core.type.AnnotationMetadata
import org.springframework.core.type.filter.AssignableTypeFilter
import org.springframework.stereotype.Component

@Configuration
class MyOqlInitConfig : BeanPostProcessor {
    companion object {
        private var inited = false;
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
        if (inited == false) {
            inited = true;

            init_app();
        }

        return super.postProcessBeforeInitialization(bean, beanName)
    }

    /**
     * 在所有Bean初始化之前执行
     */
    private fun init_app() {
    }


    @EventListener
    fun app_started(ev: ApplicationStartedEvent) {

        val flyways = SpringUtil.getBeanWithNull(FlywayBeanProcessor::class.java)
        if (flyways != null) {
            flyways.playFlyVersion();
        }

        db.mongo.groups.map { it::class.java.simpleName }.apply {
            if( this.any()){
                logger.Important("mongo groups:" + this                        .joinToString())
            }
        }

        db.sql.groups.map { it::class.java.simpleName }.apply {
            if( this.any()){
                logger.Important("sql groups:" + this                        .joinToString())
            }
        }
    }
}
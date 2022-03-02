package nbcp

import nbcp.bean.*
import nbcp.comm.Important
import nbcp.db.db
import nbcp.utils.SpringUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.annotation.*
import org.springframework.context.event.EventListener

@Import(FlywayBeanProcessor::class)
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
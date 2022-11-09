package nbcp.myoql

import nbcp.base.comm.config
import nbcp.base.extend.AsBoolean
import nbcp.base.extend.Important
import nbcp.base.utils.SpringUtil
import nbcp.myoql.bean.MongoFlywayBeanProcessor
import nbcp.myoql.db.db
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.event.EventListener

@Import(MongoFlywayBeanProcessor::class)
@Configuration
class MyOqlInitConfig {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    @EventListener
    fun app_started(ev: ApplicationStartedEvent) {

        if (config.getConfig("app.flyway.enable", "true").AsBoolean(true)) {
            val flyways = SpringUtil.getBeanWithNull(MongoFlywayBeanProcessor::class.java)
            if (flyways != null) {
                flyways.playFlyVersion();
            }
        }

        db.mongo.groups.map { it::class.java.simpleName }.apply {
            if (this.any()) {
                logger.Important("mongo groups: " + this.joinToString(","))
            }
        }

        db.sql.groups.map { it::class.java.simpleName }.apply {
            if (this.any()) {
                logger.Important("sql groups: " + this.joinToString(","))
            }
        }
    }
}
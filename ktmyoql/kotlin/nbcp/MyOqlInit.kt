package nbcp

import nbcp.db.mongo.MongoEntityEvent
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component

@Component
class MyOqlInit : ApplicationListener<ContextRefreshedEvent> {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    override fun onApplicationEvent(p0: ContextRefreshedEvent) {
        logger.warn("MongoEntityEvent.groups:" + MongoEntityEvent.groups.map { it::class.java.simpleName }.joinToString())
    }
}
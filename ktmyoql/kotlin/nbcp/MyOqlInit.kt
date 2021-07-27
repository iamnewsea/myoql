package nbcp

import nbcp.comm.LogScope
import nbcp.comm.usingScope
import nbcp.utils.*
import nbcp.db.*
import nbcp.db.mongo.event.*
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.DependsOn
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class MyOqlInit {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    @EventListener
    fun onApplicationEvent(event: ContextRefreshedEvent) {
        logger.info("mongo groups:" + db.mongo.groups.map { it::class.java.simpleName }.joinToString())
        logger.info("sql groups:" + db.sql.groups.map { it::class.java.simpleName }.joinToString())

//        var restClient = SpringUtil.getBean<RestClient>()
    }
}
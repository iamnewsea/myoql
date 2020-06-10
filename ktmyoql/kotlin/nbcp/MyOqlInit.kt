package nbcp

import nbcp.comm.clazzesIsSimpleDefine
import nbcp.utils.*
import nbcp.db.db
import nbcp.db.mongo.MongoEntityEvent
import org.bson.types.ObjectId
import org.elasticsearch.client.RestClient
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.DependsOn
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component

@Component
@DependsOn("springUtil")
class MyOqlInit : ApplicationListener<ContextRefreshedEvent> {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    override fun onApplicationEvent(p0: ContextRefreshedEvent) {

        logger.warn("mongo groups:" + db.mongo.groups.map { it::class.java.simpleName }.joinToString())
        logger.info("sql groups:" + db.sql.groups.map { it::class.java.simpleName }.joinToString())


//        var restClient = SpringUtil.getBean<RestClient>()
    }
}
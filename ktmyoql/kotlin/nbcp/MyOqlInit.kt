package nbcp

import nbcp.db.mongo.MongoEntityEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component

@Component
class MyOqlInit : ApplicationListener<ContextRefreshedEvent> {
    override fun onApplicationEvent(p0: ContextRefreshedEvent) {
        MongoEntityEvent.groups.forEach {
            println(it::class.java.simpleName)
        }
    }
}
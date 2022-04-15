package nbcp.comm

import org.springframework.boot.context.event.ApplicationStartingEvent
import org.springframework.context.ApplicationListener

/**
 * 配置项
 */
class AppStartEvent : ApplicationListener<ApplicationStartingEvent> {
    override fun onApplicationEvent(event: ApplicationStartingEvent) {
        
    }
}
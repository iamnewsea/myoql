package nbcp.embed

import org.springframework.boot.context.event.ApplicationStartingEvent
import org.springframework.context.ApplicationListener

/**
 * 程序的最高事件。
 */
class AppStartEvent : ApplicationListener<ApplicationStartingEvent> {
    override fun onApplicationEvent(event: ApplicationStartingEvent) {
 
        //该方法在环境初始化之前， 可以使额外的 bootstrap- 项生效！
//        event.springApplication.setAdditionalProfiles("k8s")
    }
}
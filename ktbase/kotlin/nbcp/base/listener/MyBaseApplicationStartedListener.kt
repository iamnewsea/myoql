package nbcp.base.listener

import org.springframework.boot.context.event.*
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component


@Component
class MyBaseApplicationStartedListener : ApplicationListener<ApplicationStartedEvent> {
    override fun onApplicationEvent(event: ApplicationStartedEvent) {
    }
}
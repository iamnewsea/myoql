package nbcp.base

import org.springframework.boot.context.event.*
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component


@Component
class MyBaseApplicationStarted : ApplicationListener<ApplicationStartedEvent> {
    override fun onApplicationEvent(event: ApplicationStartedEvent) {
    }
}
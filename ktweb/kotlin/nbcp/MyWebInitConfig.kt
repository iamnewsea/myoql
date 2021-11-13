package nbcp

import nbcp.base.service.*
import nbcp.config.MySwaggerConfig
import nbcp.base.filter.MyAllFilter
import nbcp.base.filter.MyOqlCrossFilter
import org.springframework.boot.context.event.ApplicationPreparedEvent
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.event.EventListener

@Configuration
class MyWebInitConfig {

    @EventListener
    fun prepared(ev: ApplicationPreparedEvent) {

    }
}
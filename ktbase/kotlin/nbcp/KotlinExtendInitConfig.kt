package nbcp

import nbcp.comm.GroupLogAopService
import nbcp.comm.LogLevelAopService
import nbcp.config.TaskConfig
import nbcp.utils.SpringUtil
import org.springframework.boot.context.event.ApplicationPreparedEvent
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component


@Configuration
@Import(
    value = [
        SpringUtil::class,
        ObjectMapperConfiguration::class
    ]
)
class KotlinExtendInitConfig {
    @EventListener
    fun prepared(ev: ApplicationPreparedEvent) {

    }
}
package nbcp.base.config


import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * 给 scheduler 加开关
 */
@EnableAsync
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = ["app.scheduler"], havingValue = "true", matchIfMissing = false)
class TaskConfig {
}
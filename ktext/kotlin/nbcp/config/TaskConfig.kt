package nbcp.config

import nbcp.comm.AsInt
import nbcp.comm.config
import nbcp.utils.SpringUtil
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@EnableAsync
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = ["app.scheduler"], havingValue = "true", matchIfMissing = true)
class TaskConfig {

    @Bean
    fun taskExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = config.getConfig("app.executor.core-pool-size").AsInt(3)
        executor.maxPoolSize = config.getConfig("app.executor.max-pool-size").AsInt(64)
        executor.setQueueCapacity(config.getConfig("app.executor.queue-capacity").AsInt(64))
        executor.initialize()
        return executor
    }
}
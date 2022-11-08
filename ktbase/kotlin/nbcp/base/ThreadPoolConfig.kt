package nbcp.base

import nbcp.base.config.TaskConfig
import nbcp.base.comm.*
import nbcp.base.extend.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@Configuration
@Import(TaskConfig::class)
class ThreadPoolConfig {
    @Bean
    fun myoqlTaskExecutor(): ThreadPoolTaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = config.getConfig("app.executor.core-pool-size").AsInt(3)
        executor.maxPoolSize = config.getConfig("app.executor.max-pool-size").AsInt(64)
        executor.setQueueCapacity(config.getConfig("app.executor.queue-capacity").AsInt(64))
        executor.initialize()
        return executor
    }
}
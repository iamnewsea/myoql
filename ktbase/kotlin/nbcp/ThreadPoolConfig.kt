package nbcp

import nbcp.comm.AsInt
import nbcp.comm.config
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@Configuration
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
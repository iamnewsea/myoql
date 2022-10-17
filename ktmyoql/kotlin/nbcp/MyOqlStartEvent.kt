package nbcp

import nbcp.comm.AsString
import nbcp.comm.Important
import nbcp.comm.config
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent
import org.springframework.boot.context.event.ApplicationStartingEvent
import org.springframework.context.ApplicationContextAware
import org.springframework.context.ApplicationListener

/**
 * 程序的最高事件。
 */
class MyOqlStartEvent : ApplicationListener<ApplicationEnvironmentPreparedEvent> {
    init {
        config.logoLoaded = true;
    }

    override fun onApplicationEvent(event: ApplicationEnvironmentPreparedEvent) {
        val env = event.environment

        if (logoLoaded == false) {
            logoLoaded = true;

            logger.warn(
                """
﹎﹍﹎﹍﹎﹍﹎﹍﹎﹍﹎﹍﹎﹍﹎﹍﹎
   ┌┬┐ ┬ ┬ ┌─┐ ┌─┐  ┬
   │││ └┬┘ │ │ │─┼┐ │
   ┴ ┴  ┴  └─┘ └─┘└ ┴─┘
﹊﹉﹊﹉﹊﹉﹊﹉﹊﹉﹊﹉﹊﹉﹊﹉﹊
${env.getProperty("spring.application.name")}  ${env.activeProfiles.joinToString()}
"""
            )
        }

    }

    companion object {
        var logoLoaded = false;
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }
}
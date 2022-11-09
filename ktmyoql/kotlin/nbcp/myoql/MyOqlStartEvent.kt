package nbcp.myoql

import nbcp.base.comm.config
import nbcp.base.extend.AsBooleanWithNull
import nbcp.base.extend.AsString
import nbcp.base.extend.HasValue
import nbcp.base.extend.Important
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent
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
            val list = mutableListOf<String>()
            list.add(env.getProperty("spring.application.name").AsString())
            list.add(env.activeProfiles?.joinToString(",").AsString())

            env.getProperty("app.scheduler").AsBooleanWithNull()
                .apply {
                    if (this === null || this) {
                        list.add("@EnableScheduling")
                    }
                }

            logger.Important(
                """
    ﹎﹍﹎﹍﹎﹍﹎﹍﹎﹍﹎﹍﹎﹍﹎
        ╔╦╗┬ ┬┌─┐┌─┐ ┬  
        ║║║└┬┘│ ││─┼┐│  
        ╩ ╩ ┴ └─┘└─┘└┴─┘
    ﹊﹉﹊﹉﹊﹉﹊﹉﹊﹉﹊﹉﹊﹉﹊
${list.filter { it.HasValue }.joinToString("  ")}
"""
            )
        }

    }

    companion object {
        var logoLoaded = false;
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }
}
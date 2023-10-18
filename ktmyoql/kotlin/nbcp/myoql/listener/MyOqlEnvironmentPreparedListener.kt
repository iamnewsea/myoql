package nbcp.myoql.listener

import nbcp.base.comm.config
import nbcp.base.enums.AlignDirectionEnum
import nbcp.base.extend.*
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent
import org.springframework.context.ApplicationListener

/**
 * 程序的最高事件。
 */
class MyOqlEnvironmentPreparedListener : ApplicationListener<ApplicationEnvironmentPreparedEvent> {
    init {
        config.logoLoaded = true;
    }

    override fun onApplicationEvent(event: ApplicationEnvironmentPreparedEvent) {
        val env = event.environment

        if (logoLoaded == false) {
            logoLoaded = true;
            val list = mutableListOf<String>()
            list.add(env.getProperty("spring.application.name").AsString())
            (env.activeProfiles?.toList() ?: listOf()).also {
                if (it.HasValue) {
                    list.add("profiles:" + env.activeProfiles?.joinToString(",").AsString())
                }
            }

            var title = list.filter { it.HasValue }
                .let {
                    if (it.any()) {
                        return@let """${list.joinToString("  ")}
""";
                    }
                    return@let "";
                }

            logger.Important(
                """
        ╔╦╗┬ ┬┌─┐┌─┐ ┬      
        ║║║└┬┘│ ││─┼┐│      
        ╩ ╩ ┴ └─┘└─┘└┴─┘    
${title}
""".Slice(1, -2).WrapByRectangle(AlignDirectionEnum.CENTER)
            )
        }

    }

    companion object {
        var logoLoaded = false;
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }
}
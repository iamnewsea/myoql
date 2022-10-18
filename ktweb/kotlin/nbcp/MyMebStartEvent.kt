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
class MyMebStartEvent : ApplicationListener<ApplicationEnvironmentPreparedEvent> {
    init {
        MyMvcStartEvent.logoLoaded = true;
        MyOqlStartEvent.logoLoaded = true;
    }
    override fun onApplicationEvent(event: ApplicationEnvironmentPreparedEvent) {
        val env = event.environment

        if (logoLoaded == false) {
            logoLoaded = true;

            /**
             * 如果要关闭这个日志,日志级别的设定,只能在 bootstrap.yaml 或 application.yaml 中, 在最高的时机设置!
             */
            logger.Important(
                """
    ﹎﹍﹎﹍﹎﹍﹎﹍﹎﹍﹎﹍﹎﹍﹎﹍﹎
    ╔╦╗┬ ┬┌─┐┌─┐ ┬    ╦ ╦┌─┐┌┐ 
    ║║║└┬┘│ ││─┼┐│    ║║║├┤ ├┴┐
    ╩ ╩ ┴ └─┘└─┘└┴─┘  ╚╩╝└─┘└─┘
    ﹊﹉﹊﹉﹊﹉﹊﹉﹊﹉﹊﹉﹊﹉﹊﹉﹊
${env.getProperty("spring.application.name")}  ${env.activeProfiles.joinToString(",")}
"""
            )
        }

    }

    companion object {
        private var logoLoaded = false;
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }
}
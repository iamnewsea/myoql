package nbcp.web

import nbcp.mvc.MyMvcStartEvent
import nbcp.myoql.MyOqlStartEvent
import nbcp.base.comm.*;
import nbcp.base.db.*;
import nbcp.base.enums.*;
import nbcp.base.extend.*;
import nbcp.base.utils.*;
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent
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

            val list = mutableListOf<String>()
            list.add(env.getProperty("spring.application.name").AsString())
            list.add(env.activeProfiles?.joinToString(",").AsString())

            env.getProperty("app.scheduler").AsBooleanWithNull()
                .apply {
                    if (this === null || this) {
                        list.add("@EnableScheduling")
                    }
                }

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
${list.filter { it.HasValue }.joinToString("  ")}
"""
            )
        }

    }

    companion object {
        private var logoLoaded = false;
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }
}
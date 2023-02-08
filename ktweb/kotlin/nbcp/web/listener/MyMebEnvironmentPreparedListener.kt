package nbcp.web.listener

import nbcp.base.enums.AlignDirectionEnum
import nbcp.base.extend.AsString
import nbcp.base.extend.HasValue
import nbcp.base.extend.Important
import nbcp.base.extend.WrapByRectangle
import nbcp.mvc.listener.MyMvcEnvironmentPreparedEventListener
import nbcp.myoql.listener.MyOqlEnvironmentPreparedListener
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent
import org.springframework.context.ApplicationListener

/**
 * 程序的最高事件。
 */
class MyMebEnvironmentPreparedListener : ApplicationListener<ApplicationEnvironmentPreparedEvent> {
    init {
        MyMvcEnvironmentPreparedEventListener.logoLoaded = true;
        MyOqlEnvironmentPreparedListener.logoLoaded = true;
    }

    override fun onApplicationEvent(event: ApplicationEnvironmentPreparedEvent) {
        val env = event.environment

        if (logoLoaded == false) {
            logoLoaded = true;

            val list = mutableListOf<String>()
            list.add(env.getProperty("spring.application.name").AsString())
            list.add(env.activeProfiles?.joinToString(",").AsString())

            /**
             * 如果要关闭这个日志,日志级别的设定,只能在 bootstrap.yaml 或 application.yaml 中, 在最高的时机设置!
             */
            logger.Important(
                    """
    ╔╦╗┬ ┬┌─┐┌─┐ ┬    ╦ ╦┌─┐┌┐     
    ║║║└┬┘│ ││─┼┐│    ║║║├┤ ├┴┐    
    ╩ ╩ ┴ └─┘└─┘└┴─┘  ╚╩╝└─┘└─┘    
${list.filter { it.HasValue }.joinToString("  ")}
""".WrapByRectangle(AlignDirectionEnum.CENTER)
            )
        }

    }

    companion object {
        private var logoLoaded = false;
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }
}
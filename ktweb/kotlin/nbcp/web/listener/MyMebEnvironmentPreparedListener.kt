package nbcp.web.listener

import nbcp.base.enums.AlignDirectionEnum
import nbcp.base.extend.*
import nbcp.mvc.listener.MyMvcEnvironmentPreparedEventListener
import nbcp.myoql.listener.MyOqlEnvironmentPreparedListener
import nbcp.web.comm.LoginUserParameterBeanProcessor
import nbcp.web.feign.FeignResponseConfig
import nbcp.web.feign.FeignTransferHeaderInterceptor
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Component

/**
 * 程序的最高事件。
 */
@Import(
        value = [
            LoginUserParameterBeanProcessor::class,
            FeignTransferHeaderInterceptor::class,
            FeignResponseConfig::class
        ]
)
@Component
@EnableFeignClients("nbcp.web.feign")
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
            (env.activeProfiles?.toList() ?: listOf()).also {
                if (it.HasValue) {
                    list.add("profiles:" + env.activeProfiles?.joinToString(",").AsString())
                }
            }

            env.getProperty("server.port").AsString().also {
                if (it.HasValue) {
                    list.add("port≈" + it)
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

            /**
             * 如果要关闭这个日志,日志级别的设定,只能在 bootstrap.yaml 或 application.yaml 中, 在最高的时机设置!
             */
            logger.Important(
                    """
    ╔╦╗┬ ┬┌─┐┌─┐ ┬    ╦ ╦┌─┐┌┐     
    ║║║└┬┘│ ││─┼┐│    ║║║├┤ ├┴┐    
    ╩ ╩ ┴ └─┘└─┘└┴─┘  ╚╩╝└─┘└─┘    
${title}
""".Slice(1, -2).WrapByRectangle(AlignDirectionEnum.CENTER)
            )
        }

    }

    companion object {
        private var logoLoaded = false;
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }
}
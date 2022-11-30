package nbcp.base.config


import nbcp.base.extend.Important
import nbcp.base.extend.IsCollectionType
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.SchedulingConfigurer
import org.springframework.scheduling.config.ScheduledTaskRegistrar


/**
 * 给 scheduler 加开关
 */
@Configuration
@ConditionalOnProperty(name = ["app.schedule.off"])
class ScheduleOffConfigurer : SchedulingConfigurer {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    override fun configureTasks(taskRegistrar: ScheduledTaskRegistrar) {

        taskRegistrar.javaClass.declaredFields
            .forEach { f ->
                if (f.type.IsCollectionType) {
                    f.isAccessible = true
                    val o = f.get(taskRegistrar) as MutableList<Any>?
                    if (o != null) {
                        //清除所有扫描到的定时任务
                        o.clear();
                    }
                }
            }

        logger.Important("~~-~~-~~-~~  Turn Off Scheduling !!! ~~-~~-~~-~~ ")
    }
}
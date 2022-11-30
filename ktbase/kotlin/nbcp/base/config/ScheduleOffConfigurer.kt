package nbcp.base.config


import nbcp.base.comm.config
import nbcp.base.extend.AsBoolean
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
class ScheduleOffConfigurer : SchedulingConfigurer {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    override fun configureTasks(taskRegistrar: ScheduledTaskRegistrar) {
        if (config.getConfig("app.scheduler").AsBoolean(true)) {
            return;
        }

        var cleared = false;
        taskRegistrar.javaClass.declaredFields
            .forEach { f ->
                if (f.type.IsCollectionType) {
                    f.isAccessible = true
                    val v = f.get(taskRegistrar);
                    if (v == null) {
                        return@forEach
                    }
                    if (v is MutableSet<*> && v.any()) {
                        v.clear();
                        cleared = true;
                    } else if (v is MutableList<*> && v.any()) {
                        v.clear();
                        cleared = true;
                    } else if (v is HashMap<*, *> && v.any()) {
                        v.clear();
                    }
                }
            }

        if (cleared) {
            logger.Important("""
~~-~~-~~-~~  Clear All Scheduling !!! ~~-~~-~~-~~ 
""")
        }
    }
}
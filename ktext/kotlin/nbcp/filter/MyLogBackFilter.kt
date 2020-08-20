package nbcp.filter

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.turbo.TurboFilter
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply
import nbcp.app.ScheduledTaskScope
import nbcp.comm.*
import nbcp.utils.*
import org.slf4j.Marker


/**
 * 前置过滤器，排除定时任务
 * logback-spring.xml 文件中，
 * configuration 下面添加 <turboFilter class="nbcp.filter.MyLogBackFilter"></turboFilter>
 * Filter<ILoggingEvent> 是后置过滤器
 */
class MyLogBackFilter : TurboFilter() {
    companion object {
    }

    override fun decide(marker: Marker?, logger: Logger?, level: Level?, format: String?, params: Array<out Any>?, t: Throwable?): FilterReply {
        if (level == null) {
            return FilterReply.NEUTRAL
        }

        var log = scopes.getLatestScope<LogScope>()
        if (log != null) {
            if (level.levelInt >= log.level) {
                return FilterReply.ACCEPT
            }
            return FilterReply.DENY;
        }

        //在获取 debug 期间，禁用Log
        using(LogScope(Level.OFF_INT)) {
            if (config.debug) {
                return FilterReply.ACCEPT
            }
        }
        return FilterReply.NEUTRAL
    }
}

/**
 * 应用日志过滤器
 * logback-spring.xml 文件中，
 * configuration.appender 下面添加
 * <filter class="nbcp.filter.MyAppLogBackFilter"></filter>
 */
class MyAppLogBackFilter : Filter<ILoggingEvent>() {
    companion object {
    }

    override fun decide(event: ILoggingEvent?): FilterReply{
        var taskScope = scopes.getLatestScope<ScheduledTaskScope>();
        if (taskScope != null) {
            return FilterReply.DENY;
        }

        return  FilterReply.NEUTRAL;
    }
}

/**
 * 定时任务日志过滤器，定时任务使用 ScheduledTask 注解
 * logback-spring.xml 文件中，
 * configuration.appender 下面添加
 * <filter class="nbcp.filter.MyScheduledTaskLogBackFilter"></filter>
 */
class MyScheduledTaskLogBackFilter : Filter<ILoggingEvent>() {
    companion object {
    }

    override fun decide(event: ILoggingEvent?): FilterReply{
        var taskScope = scopes.getLatestScope<ScheduledTaskScope>();
        if (taskScope == null) {
            return FilterReply.DENY;
        }

        return  FilterReply.NEUTRAL;
    }
}
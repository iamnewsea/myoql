package nbcp.filter

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.turbo.TurboFilter
import ch.qos.logback.core.filter.AbstractMatcherFilter
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply
import nbcp.app.GroupLog
import nbcp.comm.*
import nbcp.utils.*
import org.slf4j.MDC
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

    override fun decide(
        marker: Marker?,
        logger: Logger?,
        level: Level?,
        format: String?,
        params: Array<out Any>?,
        t: Throwable?
    ): FilterReply {
        if (level == null) {
            return FilterReply.NEUTRAL
        }

        //如果指定了关闭
        if (level == Level.OFF) {
            return FilterReply.DENY;
        }

        var log = scopes.GetLatest<LogScope>()
        if (log != null) {
            if (level.levelInt >= Level.toLevel(log.name).levelInt) {
                return FilterReply.ACCEPT
            }
            return FilterReply.DENY;
        }

        usingScope(LogScope.off) {
            //config.debug 本身也会调用 decide.
            if (config.debug) {
                return FilterReply.ACCEPT
            }
        }
        return FilterReply.NEUTRAL
    }
}


///**
// * 应用日志过滤器
// * logback-spring.xml 文件中，
// * configuration.appender 下面添加
// * <filter class="nbcp.filter.MyAppLogBackFilter"></filter>
// */
//class MyAppLogBackFilter : Filter<ILoggingEvent>() {
//    companion object {
//    }
//
//    override fun decide(event: ILoggingEvent?): FilterReply {
//        var taskScope = scopes.GetLatest<GroupLog>();
//        if (taskScope != null) {
//            return FilterReply.DENY;
//        }
//
//        return FilterReply.NEUTRAL;
//    }
//}

///**
// * 定时任务日志过滤器，定时任务使用 GroupLog("group") 进行注解
// * logback-spring.xml 文件中，
// * configuration.appender 下面添加
// * <filter class="nbcp.filter.MyTaskGroupLogBackFilter"></filter>
// */
//class MyTaskGroupLogBackFilter : Filter<ILoggingEvent>() {
//    companion object {
//    }
//
//    override fun decide(event: ILoggingEvent?): FilterReply {
//        var groupScope = scopes.GetLatest<GroupLog>();
//        if (groupScope == null) {
//            return FilterReply.DENY;
//        }
//
//        if (groupScope.value != "task") {
//            return FilterReply.DENY;
//        }
//        return FilterReply.NEUTRAL;
//    }
//}


/**
 * 主要业务日志分组,使用 GroupLog("main") 进行注解
 * logback-spring.xml 文件中，
 * configuration.appender 下面添加
 * <filter class="nbcp.filter.MyMainGroupLogBackFilter">
 *     <group>main</group>
 * </filter>
 *
 * 配置: app.def-all-scope-log 表示默认日志文件接受所有分组。
 */
class MyGroupLogBackFilter : Filter<ILoggingEvent>() {
    var group: String = "";

    override fun decide(event: ILoggingEvent?): FilterReply {
        var groupScope = scopes.GetLatest<GroupLog>();
        if (groupScope != null) {
            return if (groupScope.value == group) FilterReply.ACCEPT else FilterReply.DENY
        }


        if (group.HasValue) {
            return FilterReply.DENY
        }

        if (config.defAllScopeLog) {
            return FilterReply.ACCEPT
        } else {
            return FilterReply.DENY
        }
    }
}
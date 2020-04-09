package nbcp.filter

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.turbo.TurboFilter
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply
import nbcp.base.extend.*
import nbcp.base.utils.SpringUtil
import org.slf4j.Marker
import org.springframework.stereotype.Component

//class MyLogBackFilter : Filter<ILoggingEvent>() {
//    override fun decide(event: ILoggingEvent?): FilterReply {
//        println("decide:" + event?.message)
//        return FilterReply.DENY
//        if (SpringUtil.isInited && SpringUtil.context.environment.getProperty("debug").AsBoolean()) {
//            return FilterReply.ACCEPT
//        }
//
//        var log = scopes.getLatestScope(LogScope.ImportantLog, LogScope.NoLog)
//        if (log == LogScope.ImportantLog) {
//            return FilterReply.ACCEPT
//        }
//
//        if (event == null || event.level == Level.ERROR) {
//            return FilterReply.NEUTRAL
//        }
//
//        if (log == LogScope.NoLog) {
//            return FilterReply.DENY
//        }
//
//        if (event == null || event.level == null || SpringUtil.isInited == false) {
//            return FilterReply.NEUTRAL
//        }
//
//        return FilterReply.NEUTRAL
//    }
//}


/**
 * 前置过滤器
 * logback-spring.xml 文件中，
 * configuration 下面添加 <turboFilter class="nbcp.filter.MyLogBackFilter"></turboFilter>
 * Filter<ILoggingEvent> 是后置过滤器
 */
class MyLogBackFilter : TurboFilter() {
    companion object {
        private var _debug: Boolean? = null
        private val debug: Boolean
            get() {
                if (_debug != null) return _debug!!;
                if (SpringUtil.isInited == false) return false;
                _debug = SpringUtil.context.environment.getProperty("debug").AsBoolean()
                return _debug!!;
            }
    }

    override fun decide(marker: Marker?, logger: Logger?, level: Level?, format: String?, params: Array<out Any>?, t: Throwable?): FilterReply {
        if (level == null) {
            return FilterReply.NEUTRAL
        }

        var log = scopes.getLatestScope<LogScope>()
        if (log != null) {
            if (log.level >= level.levelInt) {
                return FilterReply.ACCEPT
            }
            return FilterReply.DENY;
        }

        using(LogScope.LogOff) {
            if (debug) {
                return FilterReply.ACCEPT
            }
        }
        return FilterReply.NEUTRAL
    }
}
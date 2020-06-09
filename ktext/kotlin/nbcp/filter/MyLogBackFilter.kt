package nbcp.filter

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.turbo.TurboFilter
import ch.qos.logback.core.spi.FilterReply
import nbcp.comm.*
import nbcp.utils.*
import org.slf4j.Marker

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
                _debug = config.debug;
                return _debug!!;
            }
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

        using(LogScope(Level.OFF_INT)) {
            if (debug) {
                return FilterReply.ACCEPT
            }
        }
        return FilterReply.NEUTRAL
    }
}
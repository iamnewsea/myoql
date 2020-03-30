package nbcp.filter

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply
import nbcp.base.extend.*
import nbcp.base.utils.SpringUtil

class MyLogBackFilter : Filter<ILoggingEvent>() {
    override fun decide(event: ILoggingEvent?): FilterReply {
        if (scopes.getLatestScope(LogScope.ImportantLog) != null) {
            return FilterReply.ACCEPT
        }

        if (event == null || event.level == Level.ERROR ){
            return FilterReply.NEUTRAL
        }

        if( scopes.getLatestScope(LogScope.NoLog) != null){
            return FilterReply.DENY
        }

        if (event == null || event.level == null || SpringUtil.isInited == false) {
            return FilterReply.NEUTRAL
        }

        return FilterReply.NEUTRAL
    }
}
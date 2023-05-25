package nbcp.base.filter

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply
import nbcp.base.comm.config
import nbcp.base.extend.HasValue
import nbcp.base.extend.scopes
import nbcp.base.scope.GroupLogScope

/**
 * 主要业务日志分组,使用 GroupLog("main") 进行注解
 * logback-spring.xml 文件中，
 * configuration.appender 下面添加
 * <filter class="nbcp.base.filter.MyMainGroupLogBackFilter">
 *     <group>main</group>
 * </filter>
 *
 * 配置: app.def-all-scope-log 表示默认日志文件接受所有分组。
 */
class MyGroupLogBackFilter : Filter<ILoggingEvent>() {
    var group: String = "";

    override fun decide(event: ILoggingEvent?): FilterReply {
        val groupScope = scopes.getLatest<GroupLogScope>();
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
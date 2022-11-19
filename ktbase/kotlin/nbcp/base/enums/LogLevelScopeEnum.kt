package nbcp.base.enums


import ch.qos.logback.classic.Level
import nbcp.base.scope.IScopeData

/**
 * value = Level.toLevel识别的参数，不区分大小写，如：all|trace|debug|info|warn|error|off
 */
enum class LogLevelScopeEnum(val value: Int) : IScopeData {
    ALL(Level.ALL_INT),
    TRACE(Level.TRACE_INT),
    DEBUG(Level.DEBUG_INT),
    INFO(Level.INFO_INT),
    WARN(Level.WARN_INT),
    ERROR(Level.ERROR_INT),
    OFF(Level.OFF_INT);
}
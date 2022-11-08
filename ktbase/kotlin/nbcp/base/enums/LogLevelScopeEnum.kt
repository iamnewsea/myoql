package nbcp.base.enums


import ch.qos.logback.classic.Level
import nbcp.base.scope.IScopeData

/**
 * value = Level.toLevel识别的参数，不区分大小写，如：all|trace|debug|info|warn|error|off
 */
enum class LogLevelScopeEnum(val value: Int) : IScopeData {
    all(Level.ALL_INT),
    trace(Level.TRACE_INT),
    debug(Level.DEBUG_INT),
    info(Level.INFO_INT),
    warn(Level.WARN_INT),
    error(Level.ERROR_INT),
    off(Level.OFF_INT);
}
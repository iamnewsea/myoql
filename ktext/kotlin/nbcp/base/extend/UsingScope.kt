package nbcp.base.extend

import ch.qos.logback.classic.Level


interface IDisposeable {
    fun dispose();
}

/**
 * TRACE < DEBUG < INFO < WARN < ERROR
 */
enum class LogScope(val level: Int) : IDisposeable {
    LogAllLevel(Level.TRACE_INT),
    LogDebugLevel(Level.DEBUG_INT),
    LogInfoLevel(Level.INFO_INT),
    LogWarnLevel(Level.WARN_INT),
    LogErrorLevel(Level.ERROR_INT),
    LogOff(Level.OFF_INT);

    override fun dispose() {
    }
}


enum class OrmLogScope(val remark: String) : IDisposeable {
    IgnoreExecuteTime("不记录执行时间"),
    IgnoreAffectRow("不记录影响行数");

    override fun dispose() {
    }
}




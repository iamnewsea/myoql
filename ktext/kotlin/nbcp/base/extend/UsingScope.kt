package nbcp.comm

import ch.qos.logback.classic.Level

/**
 * 提供析构的接口
 */
interface IDisposeable {
    fun dispose();
}

/**
 * TRACE < DEBUG < INFO < WARN < ERROR
 */
class LogScope(val level: Int) : IDisposeable {
    override fun dispose() {
    }
}


enum class OrmLogScope(val remark: String) : IDisposeable {
    IgnoreExecuteTime("不记录执行时间"),
    IgnoreAffectRow("不记录影响行数");

    override fun dispose() {
    }
}




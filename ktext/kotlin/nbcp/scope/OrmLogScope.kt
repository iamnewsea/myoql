package nbcp.scope

import java.io.Closeable

enum class OrmLogScope(val remark: String) : IScopeData, Closeable {
    IgnoreExecuteTime("不记录执行时间"),
    IgnoreAffectRow("不记录影响行数");

    override fun close() {
    }
}


package nbcp.base.extend


interface IDisposeable {
    fun dispose();
}

enum class LogScope(val remark: String) : IDisposeable {
    NoLog("不记录日志"),
    ImportantLog("重要日志"),
    ExecuteTimeNoLog("不记录执行时间"),
    AffectRowNoLog("不记录影响行数"),
    FilterNoLog("Filter中不记录Log");   //MyAllFilter不记录任务日志

    override fun dispose() {
    }
}




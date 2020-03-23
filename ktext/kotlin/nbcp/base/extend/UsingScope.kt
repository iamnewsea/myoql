package nbcp.base.extend


interface IDisposeable {
    fun dispose();
}

enum class LogScope(val remark:String) : IDisposeable {
    ExecuteTimeNoLog("不记录执行时间"),
    AffectRowNoLog("不记录影响行数"),
    FilterNoLog("不记录Log");   //MyAllFilter不记录任务日志

    override fun dispose() {
    }
}




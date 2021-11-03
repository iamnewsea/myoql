package nbcp.db

import nbcp.scope.IScopeData

enum class MyOqlOrmScope(val remark: String) : IScopeData {
    IgnoreCascadeUpdate("忽略级联更新"),
    IgnoreUpdateAt("忽略更新时间"),
    IgnoreExecuteTime("不记录执行时间"),
    IgnoreAffectRow("不记录影响行数");
}
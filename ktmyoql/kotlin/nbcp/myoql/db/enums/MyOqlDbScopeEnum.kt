package nbcp.myoql.db.enums;

import nbcp.base.scope.IScopeData

enum class MyOqlDbScopeEnum(val remark: String) : IScopeData {
    IGNORE_LOGICAL_DELETE("忽略逻辑更新字段"),
    IGNORE_CASCADE_UPDATE("忽略级联更新"),
    IGNORE_UPDATE_AT("忽略更新时间"),
    IGNORE_EXECUTE_TIME("不记录执行时间"),
    IGNORE_AFFECT_ROW("不记录影响行数");
}
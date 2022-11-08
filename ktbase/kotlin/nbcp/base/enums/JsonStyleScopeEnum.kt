package nbcp.base.enums

import nbcp.base.scope.IScopeData


/**
 * 递归的返回状态
 */
enum class JsonStyleScopeEnum private constructor(val mutexGroup: String) : IScopeData {
    WithNull("null"),
    IgnoreNull("null"),
    Pretty("format"),
    Compress("format"),
    DateUtcStyle("date"),       // 日期T时间Z
    DateStandardStyle("date"),  // 减号连字符
    DateLocalStyle("date");      // 斜线连字符

}
package nbcp.base.enums

import nbcp.base.scope.IScopeData


/**
 * 递归的返回状态
 */
enum class JsonStyleScopeEnum private constructor(val mutexGroup: String) : IScopeData {
    WITH_NULL("null"),
    IGNORE_NULL("null"),
    PRETTY("format"),
    COMPRESS("format"),
    DATE_UTC_STYLE("date"),       // 日期T时间Z
    DATE_STANDARD_STYLE("date"),  // 减号连字符
    DATE_LOCAL_STYLE("date");      // 斜线连字符

}
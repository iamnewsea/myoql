package nbcp.scope


/**
 * 递归的返回状态
 */
enum class JsonStyleEnumScope private constructor(val mutexGroup: String) : IScopeData {
    WithNull("null"),
    IgnoreNull("null"),
    Pretty("format"),
    Compress("format"),
    DateUtcStyle("date"),       // 日期T时间Z
    DateStandardStyle("date"),  // 减号连字符
    DateLocalStyle("date");      // 斜线连字符

}
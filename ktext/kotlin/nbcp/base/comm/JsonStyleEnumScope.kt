package nbcp.comm


/**
 * 默认：FieldStyle，IgnoreNull，Compress，DateStandardStyle
 */
enum class JsonStyleEnumScope private constructor(mutexGroup: String) {
    GetSetStyle("item"),
    FieldStyle("item"),
    WithNull("null"),
    IgnoreNull("null"),
    Pretty("format"),
    Compress("format"),
    DateUtcStyle("date"),       // 日期T时间Z
    DateStandardStyle("date"),  // 减号连字符
    DateLocalStyle("date")      // 斜线连字符
}
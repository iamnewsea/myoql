package nbcp.comm


/**
 * 默认：FieldStyle，IgnoreNull，Compress，DateStandardStyle
 */
enum class JsonStyleEnumScope {
    GetSetStyle,
    FieldStyle,
    WithNull,
    IgnoreNull,
    Pretty,
    Compress,
    DateUtcStyle,       // 日期T时间Z
    DateStandardStyle,  // 减号连字符
    DateLocalStyle      // 斜线连字符
}
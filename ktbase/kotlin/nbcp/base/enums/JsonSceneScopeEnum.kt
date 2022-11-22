package nbcp.base.enums

import nbcp.base.scope.IScopeData

/**
 * 默认使用 DB 方式：FieldStyle，IgnoreNull，Compress，DateStandardStyle
 */
enum class JsonSceneScopeEnum : IScopeData {

    /**
     * 默认方式，更精简，FieldStyle，IgnoreNull，Compress，DateStandardStyle
     */
    DB,
    WEB;
}

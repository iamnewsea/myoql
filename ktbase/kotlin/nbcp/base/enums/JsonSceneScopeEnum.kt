package nbcp.base.enums

import nbcp.base.scope.IScopeData

/**
 * 默认：FieldStyle，IgnoreNull，Compress，DateStandardStyle
 */
enum class JsonSceneScopeEnum : IScopeData {
    Web,
    Db,
    App;
}

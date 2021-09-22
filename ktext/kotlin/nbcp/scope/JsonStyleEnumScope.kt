package nbcp.scope

import nbcp.comm.scopes
import nbcp.component.AppJsonMapper
import nbcp.component.BaseJsonMapper
import nbcp.component.DbJsonMapper
import nbcp.component.WebJsonMapper
import nbcp.scope.IScopeData
import nbcp.utils.SpringUtil


/**
 * 默认：FieldStyle，IgnoreNull，Compress，DateStandardStyle
 */
enum class JsonSceneEnumScope : IScopeData {
    Web,
    Db,
    App;
}


fun JsonSceneEnumScope?.getJsonMapper(): BaseJsonMapper {
    var style: JsonSceneEnumScope? = this;
    if (style == null) {
        style = scopes.GetLatest<JsonSceneEnumScope>()
    }

    if (style == null) {
        return SpringUtil.getBean<AppJsonMapper>()
    }

    if (style == JsonSceneEnumScope.App) {
        return SpringUtil.getBean<AppJsonMapper>()
    } else if (style == JsonSceneEnumScope.Db) {
        return SpringUtil.getBean<DbJsonMapper>();
    } else if (style == JsonSceneEnumScope.Web) {
        return SpringUtil.getBean<WebJsonMapper>();
    }
    return SpringUtil.getBean<AppJsonMapper>()
}

enum class JsonStyleEnumScope private constructor(val mutexGroup: String) : IScopeData {
    WithNull("null"),
    IgnoreNull("null"),
    Pretty("format"),
    Compress("format"),
    DateUtcStyle("date"),       // 日期T时间Z
    DateStandardStyle("date"),  // 减号连字符
    DateLocalStyle("date");      // 斜线连字符

}
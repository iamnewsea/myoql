package nbcp.scope

import com.fasterxml.jackson.databind.ObjectMapper
import nbcp.comm.scopes
import nbcp.component.AppJsonMapper
import nbcp.component.DbJsonMapper
import nbcp.component.WebJsonMapper
import nbcp.utils.SpringUtil


/**
 * 默认：FieldStyle，IgnoreNull，Compress，DateStandardStyle
 */
enum class JsonSceneEnumScope : IScopeData {
    Web,
    Db,
    App;
}


fun JsonSceneEnumScope?.getJsonMapper(): ObjectMapper {
    var style: JsonSceneEnumScope? = this;
    if (style == null) {
        style = scopes.getLatest<JsonSceneEnumScope>()
    }

    if (style == null) {
        return SpringUtil.getBean<ObjectMapper>()
    }

    if (style == JsonSceneEnumScope.App) {
        return SpringUtil.getBean<AppJsonMapper>()
    } else if (style == JsonSceneEnumScope.Db) {
        return  DbJsonMapper.INSTANCE
    } else if (style == JsonSceneEnumScope.Web) {
        return  WebJsonMapper.INSTANCE
    }
    return SpringUtil.getBean<ObjectMapper>()
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
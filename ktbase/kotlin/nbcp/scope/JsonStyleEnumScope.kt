package nbcp.scope

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import nbcp.comm.initObjectMapper
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
    var scene: JsonSceneEnumScope? = this;
    if (scene == null) {
        scene = scopes.getLatest<JsonSceneEnumScope>()
    }
    var styles = scopes.getScopeTypes<JsonStyleEnumScope>()
    var withNull = styles.contains(JsonStyleEnumScope.WithNull)
    if (withNull) {
        var ret: ObjectMapper;
        if (scene == JsonSceneEnumScope.Db) {
            ret = DbJsonMapper()
        } else if (scene == JsonSceneEnumScope.Web) {
            ret = WebJsonMapper()
        } else {
            ret = AppJsonMapper();
        }

        ret.setSerializationInclusion(JsonInclude.Include.ALWAYS)
        return ret;
    }

    if (scene == JsonSceneEnumScope.Db) {
        return DbJsonMapper.INSTANCE
    } else if (scene == JsonSceneEnumScope.Web) {
        return WebJsonMapper.INSTANCE
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
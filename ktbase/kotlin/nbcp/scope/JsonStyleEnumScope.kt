package nbcp.scope

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import nbcp.comm.initObjectMapper
import nbcp.comm.scopes
import nbcp.component.AppJsonMapper
import nbcp.component.DbJsonMapper
import nbcp.component.WebJsonMapper
import nbcp.utils.SpringUtil



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


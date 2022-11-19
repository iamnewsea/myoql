package nbcp.base.scope

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import nbcp.base.component.DbJsonMapper
import nbcp.base.component.WebJsonMapper
import nbcp.base.enums.JsonSceneScopeEnum
import nbcp.base.enums.JsonStyleScopeEnum
import nbcp.base.extend.scopes


fun JsonSceneScopeEnum?.getJsonMapper(): ObjectMapper {
    var scene: JsonSceneScopeEnum? = this;
    if (scene == null) {
        scene = scopes.getLatest<JsonSceneScopeEnum>()
    }
    var styles = scopes.getScopeTypes<JsonStyleScopeEnum>()
    var withNull = styles.contains(JsonStyleScopeEnum.WITH_NULL)
    if (withNull) {
        var ret: ObjectMapper;
        if (scene == JsonSceneScopeEnum.DB) {
            ret = DbJsonMapper()
        } else {
            ret = WebJsonMapper()
        }

        ret.setSerializationInclusion(JsonInclude.Include.ALWAYS)
        return ret;
    }

    if (scene == JsonSceneScopeEnum.DB) {
        return DbJsonMapper.INSTANCE
    }
    return WebJsonMapper.INSTANCE
}


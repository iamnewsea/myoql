package nbcp.base.scope

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import nbcp.base.component.AppJsonMapper
import nbcp.base.component.DbJsonMapper
import nbcp.base.component.WebJsonMapper
import nbcp.base.enums.JsonSceneScopeEnum
import nbcp.base.enums.JsonStyleScopeEnum
import nbcp.base.extend.scopes
import nbcp.base.utils.SpringUtil


fun JsonSceneScopeEnum?.getJsonMapper(): ObjectMapper {
    var scene: JsonSceneScopeEnum? = this;
    if (scene == null) {
        scene = scopes.getLatest<JsonSceneScopeEnum>()
    }
    var styles = scopes.getScopeTypes<JsonStyleScopeEnum>()
    var withNull = styles.contains(JsonStyleScopeEnum.WithNull)
    if (withNull) {
        var ret: ObjectMapper;
        if (scene == JsonSceneScopeEnum.Db) {
            ret = DbJsonMapper()
        } else if (scene == JsonSceneScopeEnum.Web) {
            ret = WebJsonMapper()
        } else {
            ret = AppJsonMapper();
        }

        ret.setSerializationInclusion(JsonInclude.Include.ALWAYS)
        return ret;
    }

    if (scene == JsonSceneScopeEnum.Db) {
        return DbJsonMapper.INSTANCE
    } else if (scene == JsonSceneScopeEnum.Web) {
        return WebJsonMapper.INSTANCE
    }
    return SpringUtil.getBean<AppJsonMapper>()
}


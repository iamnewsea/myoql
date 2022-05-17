package nbcp.extend

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import nbcp.comm.ToJson
import nbcp.scope.JsonSceneEnumScope
import nbcp.scope.JsonStyleEnumScope
import nbcp.utils.SpringUtil

@JvmOverloads
fun <T> T.ToYaml(): String {
    return SpringUtil.getBean<YAMLMapper>().writeValueAsString(this)
}

package nbcp.extend

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import nbcp.comm.ToJson
import nbcp.component.YamlObjectMapper
import nbcp.scope.JsonSceneEnumScope
import nbcp.scope.JsonStyleEnumScope
import nbcp.utils.SpringUtil

@JvmOverloads
fun <T> T.ToYaml(): String {
    return YamlObjectMapper.INSTANCE.writeValueAsString(this)
}

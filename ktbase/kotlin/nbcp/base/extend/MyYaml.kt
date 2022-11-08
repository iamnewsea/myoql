@file:JvmName("MyHelper")
@file:JvmMultifileClass

package nbcp.base.extend


import nbcp.base.component.YamlObjectMapper


fun <T> T.ToYaml(): String {
    return YamlObjectMapper.INSTANCE.writeValueAsString(this)
}

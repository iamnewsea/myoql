package nbcp.base.component

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import nbcp.base.extend.initObjectMapper

//@Component("YamlObjectMapper")
class YamlObjectMapper private constructor() : YAMLMapper() {
    companion object {
        val INSTANCE: YamlObjectMapper by lazy {
            return@lazy YamlObjectMapper();
        }
    }

    init {
        this.initObjectMapper()
    }
}
package nbcp.base.component


import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import nbcp.base.extend.initObjectMapper


//@Component("DbJson")
class DbJsonMapper() : ObjectMapper() {
    companion object {
        val INSTANCE: DbJsonMapper by lazy {
            return@lazy DbJsonMapper();
        }
    }

    init {
        this.initObjectMapper();

        this.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
        this.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        this.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);
        this.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }
}

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
package nbcp.component

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import nbcp.extend.initObjectMapper


import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Component


@Component("DbJson")
class DbJsonMapper : ObjectMapper(), InitializingBean {
    override fun afterPropertiesSet() {
        this.initObjectMapper();

        this.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
        this.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        this.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);
        this.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }
}

@Component("YamlObjectMapper")
class YamlObjectMapper : YAMLMapper(), InitializingBean {
    override fun afterPropertiesSet() {
        this.initObjectMapper()
    }
}
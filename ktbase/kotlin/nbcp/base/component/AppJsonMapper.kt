package nbcp.base.component

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import nbcp.base.extend.initObjectMapper

class AppJsonMapper : ObjectMapper() {


    init {
        this.initObjectMapper();

        this.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
        this.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        this.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);
        this.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }
}
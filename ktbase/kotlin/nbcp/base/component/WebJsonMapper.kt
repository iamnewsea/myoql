package nbcp.base.component


import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import nbcp.base.extend.initObjectMapper


//@Component("WebJson")
class WebJsonMapper() : ObjectMapper() {
    companion object {
        val INSTANCE: WebJsonMapper by lazy {
            return@lazy WebJsonMapper();
        }
    }

    init {
        this.initObjectMapper();

        this.setSerializationInclusion(JsonInclude.Include.USE_DEFAULTS)
        this.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.DEFAULT);
        this.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.DEFAULT);
        this.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.DEFAULT);
        this.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.NONE);
    }
}
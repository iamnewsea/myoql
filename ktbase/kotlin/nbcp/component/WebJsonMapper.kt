package nbcp.component

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.*
import nbcp.comm.initObjectMapper


import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Component


//@Component("WebJson")
class WebJsonMapper private constructor(): ObjectMapper() {
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
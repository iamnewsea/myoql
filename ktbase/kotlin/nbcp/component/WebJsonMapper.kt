package nbcp.component

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule


import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Component
import java.text.SimpleDateFormat
import java.util.*


@Component("WebJson")
class WebJsonMapper : BaseJsonMapper(), InitializingBean {


    override fun afterPropertiesSet() {
        this.init();

        this.setSerializationInclusion(JsonInclude.Include.USE_DEFAULTS)
        this.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.DEFAULT);
        this.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.DEFAULT);
        this.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.DEFAULT);
        this.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.NONE);

    }

}
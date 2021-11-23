package nbcp

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

@Component
class ObjectMapperConfiguration :BeanPostProcessor {
    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
        if( bean is ObjectMapper){
            bean.configure(MapperFeature.USE_STD_BEAN_NAMING, true)
            bean.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        }
        return super.postProcessBeforeInitialization(bean, beanName)
    }
}
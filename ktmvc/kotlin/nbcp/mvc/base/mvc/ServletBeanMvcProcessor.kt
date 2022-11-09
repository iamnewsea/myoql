package nbcp.mvc.base.mvc

import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.stereotype.Component
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter

@Component
@ConditionalOnClass(RequestMappingHandlerAdapter::class)
class ServletBeanMvcProcessor : BeanPostProcessor {
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        if (bean is RequestMappingHandlerAdapter) {
            var handlerAdapter = bean;

            var listResolvers = mutableListOf<HandlerMethodArgumentResolver>()
            listResolvers.add(JsonModelParameterConverter());
            listResolvers.addAll(handlerAdapter.argumentResolvers ?: listOf())
            handlerAdapter.argumentResolvers = listResolvers;
        }
        return super.postProcessAfterInitialization(bean, beanName)
    }
}
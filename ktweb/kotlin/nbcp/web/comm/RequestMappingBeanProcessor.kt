package nbcp.web.comm

import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.stereotype.Component
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter


@Component
@ConditionalOnClass(RequestMappingHandlerAdapter::class)
class RequestMappingBeanProcessor : BeanPostProcessor {
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        if (bean is RequestMappingHandlerAdapter) {
            var handlerAdapter = bean;

            val resolvers = handlerAdapter.argumentResolvers ?: listOf()
            if (resolvers.any { it is LoginUserParameterConverter } == false) {
                var listResolvers = mutableListOf<HandlerMethodArgumentResolver>()
                listResolvers.add(LoginUserParameterConverter());
                listResolvers.addAll(resolvers)
                handlerAdapter.argumentResolvers = listResolvers;
            }
        }
        return super.postProcessAfterInitialization(bean, beanName)
    }
}
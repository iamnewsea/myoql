package nbcp.mvc.mvc

import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.stereotype.Component
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter

@Component
@ConditionalOnClass(RequestMappingHandlerAdapter::class)
class ServletBeanMvcProcessor : BeanPostProcessor {
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        if (bean is RequestMappingHandlerAdapter) {
            var handlerAdapter = bean;
            val resolvers = handlerAdapter.argumentResolvers ?: listOf()
            if (resolvers.any { it is JsonModelParameterConverter } == false) {
                var listResolvers = mutableListOf<HandlerMethodArgumentResolver>()
                listResolvers.add(JsonModelParameterConverter());
                listResolvers.addAll(handlerAdapter.argumentResolvers ?: listOf())
                handlerAdapter.argumentResolvers = listResolvers;
            }
        }
        return super.postProcessAfterInitialization(bean, beanName)
    }
}

// 这个方法，解析器只能往后加，在列表最后的优先级是最低的。所以它基本不能用。
//@Component
//@ConditionalOnClass(RequestMappingHandlerAdapter::class)
//class ServletBeanMvcProcessor : WebMvcConfigurer{
//    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
//        super.add(resolvers)
//    }
//}
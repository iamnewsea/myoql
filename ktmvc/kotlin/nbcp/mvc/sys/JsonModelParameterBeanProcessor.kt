package nbcp.mvc.sys

import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.core.Ordered
import org.springframework.stereotype.Component
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter

@Component
@ConditionalOnClass(RequestMappingHandlerAdapter::class)
class JsonModelParameterBeanProcessor : BeanPostProcessor {
    companion object {
        /**
         * 如果有 Ordered，则在前，并按 Ordered排序， 其余顺序不变
         */
        fun <T> orderWithOrderedBean(list: List<T>): List<T> {
            var map = list.groupBy { it is Ordered }
            var list1 = map.get(true)?.sortedBy { (it as Ordered).order } ?: listOf();
            var list2 = map.get(false) ?: listOf()

            return list1 + list2;
        }
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        if (bean is RequestMappingHandlerAdapter) {
            var handlerAdapter = bean;
            val resolvers = handlerAdapter.argumentResolvers ?: listOf()
            if (resolvers.any { it is JsonModelParameterConverter } == false) {
                var listResolvers = mutableListOf<HandlerMethodArgumentResolver>()
                listResolvers.add(JsonModelParameterConverter());
                listResolvers.addAll(handlerAdapter.argumentResolvers ?: listOf())


                handlerAdapter.argumentResolvers = orderWithOrderedBean(listResolvers);
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
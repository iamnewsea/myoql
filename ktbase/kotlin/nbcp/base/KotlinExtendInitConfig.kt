package nbcp.base

import nbcp.base.component.BaseImportBeanDefinitionRegistrar
import org.springframework.context.annotation.Configuration
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service


/**
 * 不能在这里 Import 任何Bean, 因为这个Bean的时机太早了, 会导致 Import 的目标对象不生效.
 */
class KotlinExtendInitConfig : BaseImportBeanDefinitionRegistrar(
    "nbcp",
    listOf(
        AnnotationTypeFilter(Service::class.java),
        AnnotationTypeFilter(Component::class.java),
        AnnotationTypeFilter(Configuration::class.java),
    )
) {


//    @EventListener
//    fun prepared(ev: ApplicationPreparedEvent) {
//        var om = SpringUtil.beanFactory.getBeansOfType(ObjectMapper::class.java);
//        if (om.size > 1 && !om.any { SpringUtil.beanFactory.getBeanDefinition(it.key).isPrimary }) {
//            SpringUtil.beanFactory.getBeanDefinition("appJsonMapper").isPrimary = true;
//        }
//    }
}
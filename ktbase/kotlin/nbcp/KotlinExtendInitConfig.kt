package nbcp

import nbcp.component.BaseImportBeanDefinitionRegistrar
import org.springframework.context.annotation.Configuration
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service


//@Configuration
class KotlinExtendInitConfig : BaseImportBeanDefinitionRegistrar(
    "nbcp",
    listOf(
        AnnotationTypeFilter(Service::class.java),
        AnnotationTypeFilter(Component::class.java),
        AnnotationTypeFilter(Configuration::class.java),
    )
) {
}
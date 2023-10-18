package nbcp.web

import nbcp.base.component.BaseImportBeanDefinitionRegistrar
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RestController

class KotlinWebExtendConfig :    BaseImportBeanDefinitionRegistrar (
        "nbcp.web",
        listOf(
                AnnotationTypeFilter(RestController::class.java),
                AnnotationTypeFilter(Service::class.java),
                AnnotationTypeFilter(Configuration::class.java),
                AnnotationTypeFilter(Component::class.java)
        )
) {
}
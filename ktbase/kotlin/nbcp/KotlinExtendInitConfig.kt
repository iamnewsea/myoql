package nbcp

import nbcp.component.BaseImportBeanDefinitionRegistrar
import nbcp.config.TaskConfig
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service


//@Configuration

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
}
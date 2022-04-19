package nbcp

import nbcp.base.flux.filter.CrossFilterConfig
import nbcp.base.mvc.filter.MyAllFilter
import nbcp.base.mvc.filter.MyOqlCrossFilter
import nbcp.component.BaseImportBeanDefinitionRegistrar
import nbcp.utils.ClassUtil
import org.reactivestreams.Publisher
import org.springframework.beans.factory.config.BeanDefinitionHolder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.context.ResourceLoaderAware
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar
import org.springframework.core.io.ResourceLoader
import org.springframework.core.type.AnnotationMetadata
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.core.type.filter.AssignableTypeFilter
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import javax.servlet.Filter


@Import(
    value = [
        KotlinWebExtendConfigServlet::class,
        KotlinWebExtendConfigWebFlux::class
    ]
)
//@Configuration
class KotlinWebExtendConfig : BaseImportBeanDefinitionRegistrar(
    "nbcp",
    listOf(
        AnnotationTypeFilter(RestController::class.java),
        AnnotationTypeFilter(Service::class.java),
        AnnotationTypeFilter(Configuration::class.java),
        AnnotationTypeFilter(Component::class.java)
    ),
    listOf(AssignableTypeFilter(KotlinWebExtendConfig::class.java))
)

package nbcp.web

import nbcp.base.component.BaseImportBeanDefinitionRegistrar
import nbcp.web.comm.LoginUserParameterBeanProcessor
import nbcp.web.fiegn.FeignResponseConfig
import nbcp.web.fiegn.FeignTransferHeaderInterceptor
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.core.type.filter.AssignableTypeFilter
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RestController

@Import(
        value = [
            LoginUserParameterBeanProcessor::class,
            FeignTransferHeaderInterceptor::class,
            FeignResponseConfig::class
        ]
)
@EnableFeignClients("nbcp.web.feign")
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
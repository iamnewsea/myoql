package nbcp.web

import nbcp.base.component.BaseImportBeanDefinitionRegistrar
import nbcp.web.base.mvc.filter.MyAllFilter
import nbcp.web.base.mvc.filter.MyOqlCrossFilter
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.core.type.filter.AssignableTypeFilter
import org.springframework.stereotype.Component

@Component
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
class KotlinWebExtendConfigServlet : BaseImportBeanDefinitionRegistrar(
    "nbcp",
    listOf(),
    listOf(
        AssignableTypeFilter(MyOqlCrossFilter::class.java),
        AssignableTypeFilter(MyAllFilter::class.java)
    )
)
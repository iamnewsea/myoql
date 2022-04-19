package nbcp

import nbcp.base.flux.filter.CrossFilterConfig
import nbcp.base.mvc.filter.MyAllFilter
import nbcp.base.mvc.filter.MyOqlCrossFilter
import nbcp.utils.ClassUtil
import org.reactivestreams.Publisher
import org.springframework.beans.factory.config.BeanDefinitionHolder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.ResourceLoaderAware
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner
import org.springframework.context.annotation.Configuration
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


//@Import(value = [
//    MyWebBeanImporter::class,
//    DefaultUserAuthenticationService::class
//])
@Component
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
class KotlinWebExtendConfigWebFlux : ImportBeanDefinitionRegistrar, ResourceLoaderAware {
    private lateinit var resourceLoader: ResourceLoader


    /**
     * 类 名: MyClassPathBeanDefinitionScanner
     * 描 述: 定义一个扫描器，指定需要扫描的标识
     *
     * @author: jiaYao
     */
    class MyWebClassPathBeanDefinitionScanner(registry: BeanDefinitionRegistry?, useDefaultFilters: Boolean) :
        ClassPathBeanDefinitionScanner(registry, useDefaultFilters) {
        /**
         * @addIncludeFilter 将自定义的注解添加到扫描任务中
         * @addExcludeFilter 将带有自定义注解的类 ，不加载到容器中
         */
        fun registerFilters() {
            addExcludeFilter(AssignableTypeFilter(CrossFilterConfig::class.java));
        }

        public override fun doScan(vararg basePackages: String): Set<BeanDefinitionHolder> {
            return super.doScan(*basePackages)
        }
    }

    /**
     * 动态置顶扫描包路径下特殊的类加载到Bean中
     * @param importingClassMetadata
     * @param registry
     */
    override fun registerBeanDefinitions(
        importingClassMetadata: AnnotationMetadata?,
        registry: BeanDefinitionRegistry?
    ) {
        // 当前MyClassPathBeanDefinitionScanner已被修改为扫描带有指定注解的类
        val scanner = MyWebClassPathBeanDefinitionScanner(registry, false)
        scanner.resourceLoader = resourceLoader
        scanner.registerFilters()
        scanner.doScan("nbcp")
    }

    override fun setResourceLoader(resourceLoader: ResourceLoader) {
        this.resourceLoader = resourceLoader
    }
}


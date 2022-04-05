package nbcp

import nbcp.base.flux.filter.CrossFilterConfig
import nbcp.base.mvc.filter.MyAllFilter
import nbcp.base.mvc.filter.MyOqlCrossFilter
import nbcp.utils.ClassUtil
import org.reactivestreams.Publisher
import org.springframework.beans.factory.config.BeanDefinitionHolder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
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
//@Configuration
class KotlinWebExtendConfig : ImportBeanDefinitionRegistrar, ResourceLoaderAware {
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

            /**
             * TODO addIncludeFilter  满足任意includeFilters会被加载
             */
            addIncludeFilter(AnnotationTypeFilter(RestController::class.java))
            addIncludeFilter(AnnotationTypeFilter(Service::class.java))
            addIncludeFilter(AnnotationTypeFilter(Configuration::class.java))
            addIncludeFilter(AnnotationTypeFilter(Component::class.java))
//        addIncludeFilter(AnnotationTypeFilter(AutoLoadBean::class.java))
            /**
             * TODO addExcludeFilter 同样的满足任意excludeFilters不会被加载
             */
            addExcludeFilter(AssignableTypeFilter(KotlinWebExtendConfig::class.java));

            try {
                if (ClassUtil.exists(Filter::class.java.name)) {
                    addExcludeFilter(AssignableTypeFilter(MyOqlCrossFilter::class.java));
                    addExcludeFilter(AssignableTypeFilter(MyAllFilter::class.java));
                }
            } catch (ex: Throwable) {
            }


            try {
                if (ClassUtil.exists(Publisher::class.java.name)) {
                    addExcludeFilter(AssignableTypeFilter(CrossFilterConfig::class.java));
                }
            } catch (ex: Throwable) {

            }
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


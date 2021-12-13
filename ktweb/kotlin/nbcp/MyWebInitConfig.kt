package nbcp

import nbcp.base.service.*
import nbcp.utils.SpringUtil
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.BeanFactoryAware
import org.springframework.beans.factory.config.BeanDefinitionHolder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.boot.context.event.ApplicationPreparedEvent
import org.springframework.context.ResourceLoaderAware
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar
import org.springframework.context.event.EventListener
import org.springframework.core.io.ResourceLoader
import org.springframework.core.type.AnnotationMetadata
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.core.type.filter.AssignableTypeFilter
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RestController


@Import(value = [MyWebBeanImporter::class, DefaultUserAuthenticationService::class])
@Configuration
class MyWebInitConfig {

    @EventListener
    fun prepared(ev: ApplicationPreparedEvent) {

    }
}


@Component
class MyWebBeanImporter : ImportBeanDefinitionRegistrar, ResourceLoaderAware, BeanFactoryAware {
    private lateinit var resourceLoader: ResourceLoader
    private lateinit var beanFactory: BeanFactory;


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
    }

    override fun setBeanFactory(beanFactory: BeanFactory) {
        this.beanFactory = beanFactory;
    }

    override fun setResourceLoader(resourceLoader: ResourceLoader) {
        this.resourceLoader = resourceLoader
    }
}


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
//        addIncludeFilter(AnnotationTypeFilter(AutoLoadBean::class.java))
        /**
         * TODO addExcludeFilter 同样的满足任意excludeFilters不会被加载
         */
        // addExcludeFilter(new AnnotationTypeFilter(MyService.class));
    }
}
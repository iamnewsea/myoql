package nbcp.base.component

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanDefinitionHolder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.context.ResourceLoaderAware
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar
import org.springframework.core.io.ResourceLoader
import org.springframework.core.type.AnnotationMetadata
import org.springframework.core.type.filter.TypeFilter
import org.springframework.stereotype.Component

abstract class BaseImportBeanDefinitionRegistrar(
        var basePackage: String,
        val includeFilters: List<TypeFilter>,
        val excludeFilters: List<TypeFilter> = listOf()
) : ImportBeanDefinitionRegistrar, ResourceLoaderAware {
    private lateinit var resourceLoader: ResourceLoader


    /**
     * 类 名: MyClassPathBeanDefinitionScanner
     * 描 述: 定义一个扫描器，指定需要扫描的标识
     *
     * @author: jiaYao
     */
    class MyOqlClassPathBeanDefinitionScanner(
            registry: BeanDefinitionRegistry, useDefaultFilters: Boolean,
            val includeFilters: List<TypeFilter>,
            val excludeFilters: List<TypeFilter>
    ) :
            ClassPathBeanDefinitionScanner(registry, useDefaultFilters) {
        /**
         * @addIncludeFilter 将自定义的注解添加到扫描任务中
         * @addExcludeFilter 将带有自定义注解的类 ，不加载到容器中
         */
        fun registerFilters() {
            /**
             * TODO addIncludeFilter  满足任意includeFilters会被加载
             */
            includeFilters.forEach {
                addIncludeFilter(it);
            }

            excludeFilters.forEach {
                addExcludeFilter(it);
            }
        }

        /**
         * 重写类扫描包路径加载器，调用父类受保护的扫描方法 doScan
         * @param basePackages
         * @return
         */
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
            importingClassMetadata: AnnotationMetadata,
            registry: BeanDefinitionRegistry
    ) {
        // 当前MyClassPathBeanDefinitionScanner已被修改为扫描带有指定注解的类
        val scanner = MyOqlClassPathBeanDefinitionScanner(registry, false, includeFilters, excludeFilters)
        scanner.resourceLoader = resourceLoader
        scanner.registerFilters()
        scanner.doScan(basePackage)

        LoggerFactory.getLogger(this::class.java).info("扫描: " + basePackage);
    }


    override fun setResourceLoader(resourceLoader: ResourceLoader) {
        this.resourceLoader = resourceLoader
    }
}
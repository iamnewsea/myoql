package nbcp.comm

import nbcp.utils.ClassUtil
import nbcp.utils.MyUtil
import nbcp.utils.SpringUtil
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.BeanFactoryAware
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DeferredImportSelector
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.ImportSelector
import org.springframework.core.type.AnnotationMetadata
import org.springframework.stereotype.Component
import java.lang.Exception
import java.lang.annotation.Inherited

/**
 * 如果项目的默认包不是 nbcp, 需要手动导入 nbcp.** 的包。 在SpringBoot启动类上定义：
 *
 * java:
 * @ComponentScan("nbcp.**")
 * @Import(value = {EnableNbcpBean.class})
 *
 * kotlin:
 * @ComponentScan("nbcp.**")
 * @Import(NbcpBeanSelector::class)
 */
class NbcpBeanSelector : DeferredImportSelector, BeanFactoryAware {
    private lateinit var beanFactory: BeanFactory;
    override fun selectImports(p0: AnnotationMetadata): Array<String> {

        var ret = ClassUtil.getClassesWithAnnotationType("nbcp", Component::class.java)
//            .sortedWith({ o1, o2 ->
//
//                if (BeanDefinitionRegistryPostProcessor::class.java.isAssignableFrom(o1)) {
//                    return@sortedWith 1;
//                } else if (BeanDefinitionRegistryPostProcessor::class.java.isAssignableFrom(o2)) {
//                    return@sortedWith -1;
//                }
//
//                if (BeanFactoryPostProcessor::class.java.isAssignableFrom(o1)) {
//                    return@sortedWith 1;
//                } else if (BeanFactoryPostProcessor::class.java.isAssignableFrom(o2)) {
//                    return@sortedWith -1;
//                }
//
//                if (BeanPostProcessor::class.java.isAssignableFrom(o1)) {
//                    return@sortedWith 1;
//                } else if (BeanPostProcessor::class.java.isAssignableFrom(o2)) {
//                    return@sortedWith -1;
//                }
//
//                return@sortedWith 0;
//            })
            .map { it.name }
            .toTypedArray();

        return ret;
    }

    override fun setBeanFactory(beanFactory: BeanFactory) {
        this.beanFactory = beanFactory;
    }
}

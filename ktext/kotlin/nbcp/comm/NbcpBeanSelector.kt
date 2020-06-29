package nbcp.comm

import nbcp.utils.MyUtil
import nbcp.utils.SpringUtil
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.BeanFactoryAware
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.ImportSelector
import org.springframework.core.type.AnnotationMetadata
import org.springframework.stereotype.Component
import java.lang.annotation.Inherited


class NbcpBeanSelector : ImportSelector, BeanFactoryAware {
    private lateinit var beanFactory: BeanFactory;
    override fun selectImports(p0: AnnotationMetadata): Array<String> {
        var anns_components = linkedMapOf<Class<*>, Boolean>();

        var ret = MyUtil.findClasses("nbcp", SpringUtil::class.java)
                .filter {
                    var anns = it.annotations;
                    if (anns.any() == false) return@filter false;
                    if (it == EnableNbcpBean::class.java) return@filter false;
                    if (anns.any { it.annotationClass.java == Component::class.java }) return@filter true;

                    for (i in (0 until anns.size)) {
                        var clazz = anns.get(i).annotationClass.java;

                        var value = anns_components.get(clazz);
                        if (value == null) {
                            value = getHasComponent(clazz);
                            anns_components.put(clazz, value);
                        }

                        if (value == true) {
                            return@filter true;
                        }
                    }

                    return@filter false;
                }
                .map { it.name }.toTypedArray();

        return ret;
    }

    private fun getHasComponent(javaClass: Class<*>): Boolean {
        return javaClass.getAnnotation(Component::class.java) != null;
    }

    override fun setBeanFactory(beanFactory: BeanFactory) {
        this.beanFactory = beanFactory;
    }
}

/**
 * 自动导入 nbcp.** 的包。
 * 需要在SpringBoot启动类上定义：
 *
 * @ComponentScan("nbcp.**")
 * @Import(value = {EnableNbcpBean.class})
 */
@Import(NbcpBeanSelector::class)
class EnableNbcpBean
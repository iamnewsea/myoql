package nbcp.comm

import nbcp.utils.ClassUtil
import nbcp.utils.MyUtil
import nbcp.utils.SpringUtil
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.BeanFactoryAware
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DeferredImportSelector
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.ImportSelector
import org.springframework.core.type.AnnotationMetadata
import org.springframework.stereotype.Component
import java.lang.Exception
import java.lang.annotation.Inherited

///**
// * 如果项目的默认包不是 nbcp, 需要手动导入 nbcp.** 的包。 在SpringBoot启动类上定义：
// *
// * @ComponentScan("nbcp.**")
// * @Import(value = {EnableNbcpBean.class})
// */
//@Import(NbcpBeanSelector::class)
class NbcpBeanSelector : DeferredImportSelector, BeanFactoryAware {
    private lateinit var beanFactory: BeanFactory;
    override fun selectImports(p0: AnnotationMetadata): Array<String> {

        var ret = ClassUtil.findClasses("nbcp", SpringUtil::class.java, {
            try {
                var anns = it.annotations;
                if (anns.any() == false) return@findClasses false;

                //如果该类注解是 Component
                if (anns.any { it.annotationClass.java == Component::class.java }) return@findClasses true;
                //如果该类注解的注解是 Component
                for (i in (0 until anns.size)) {
                    var clazz = anns.get(i).annotationClass.java;
                    if (clazz.getAnnotation(Component::class.java) != null) {
                        return@findClasses true;
                    }
                }
            } catch (e: Exception) {
                return@findClasses false
            }
            return@findClasses false;
        }).map { it.name }.toTypedArray();

        return ret;
    }

    override fun setBeanFactory(beanFactory: BeanFactory) {
        this.beanFactory = beanFactory;
    }
}

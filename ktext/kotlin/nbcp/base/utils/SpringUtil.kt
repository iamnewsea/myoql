package nbcp.base.utils

import org.slf4j.LoggerFactory
import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component


/**
 * Created by udi on 17-5-22.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
open class SpringUtil : ApplicationContextAware {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
        private var applicationContext: ApplicationContext? = null

        val context: ApplicationContext
            get() {
                if (applicationContext == null) {
                    throw Exception("ApplicationContext为空,在 @SpringBootApplication 下添加 @Import(SpringUtil::class)")
                }
                return applicationContext!!
            }

        val isInited: Boolean
            get() = applicationContext != null

        //获取applicationContext
//        fun setContext(): ApplicationContext {
//            return
//        }


        //通过name获取 Bean.
        fun getBeanObject(name: String): Any {
            return context.getBean(name);
        }


        //通过class获取Bean.
//        fun <T> getBean(collectionClass: Class<T>): T {
//            var ret = getContext().getBean(collectionClass);
//
//            return ret;
//        }

        inline fun <reified T> getBean(): T {
            return context.getBean(T::class.java);
        }


        inline fun <reified T> getBean(name: String): T {
            return context.getBean(name, T::class.java);
        }

        //通过name,以及Clazz返回指定的Bean
//        fun <T> getBean(name: String, clazz: Class<T>): T {
//            return context.getBean(name, clazz)
//        }
    }

    @Throws(BeansException::class)
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        if (applicationContext != null) {
            SpringUtil.applicationContext = applicationContext

            logger.info("成功设置 ApplicationContext .")
        } else {
            logger.error("设置 ApplicationContext 出现了空值!")
        }
    }
}
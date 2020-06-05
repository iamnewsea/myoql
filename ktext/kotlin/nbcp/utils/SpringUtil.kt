package nbcp.utils

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
        private var applicationContext: ApplicationContext? = null

        @JvmStatic
        val context: ApplicationContext
            get() {
                if (applicationContext == null) {
                    throw RuntimeException("ApplicationContext为空,在 @SpringBootApplication 下添加 @Import(SpringUtil::class)")
                }
                return applicationContext!!
            }

        @JvmStatic
        val isInited: Boolean
            get() = applicationContext != null

        //获取applicationContext
//        fun setContext(): ApplicationContext {
//            return
//        }

        @JvmStatic
        fun getBeanObjectByArgs(name: String): Any {
            return context.getBean(name);
        }

        /**
         * 通过 name 获取Bean，并传递参数
         */
        @JvmStatic
        fun getBeanObjectByArgs(name: String, vararg args: Any): Any {
            return context.getBean(name, *args);
        }


        //通过class获取Bean.
        @JvmStatic
        fun <T> getBean(clazz: Class<T>): T {
            var ret = context.getBean(clazz);
            return ret;
        }

        @JvmStatic
        inline fun <reified T> getBean(): T {
            return context.getBean(T::class.java);
        }

        /**
         * 通过 类型 获取Bean，并传递参数
         */
        @JvmStatic
        inline fun <reified T> getBeanByArgs(vararg args: Any): T {
            return context.getBean(T::class.java, *args);
        }

        @JvmStatic
        inline fun <reified T> getBeanByName(name: String): T {
            return context.getBean(name, T::class.java);
        }

        @JvmStatic
        fun <T> getBeanByName(name: String, clazz: Class<T>): T {
            return context.getBean(name, clazz);
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
        } else {
            throw RuntimeException("设置 ApplicationContext 出现了异常!")
        }
    }
}
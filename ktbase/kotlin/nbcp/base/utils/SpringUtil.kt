package nbcp.base.utils

import nbcp.base.extend.Important
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.beans.factory.support.GenericBeanDefinition
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.Primary
import org.springframework.core.PriorityOrdered
import org.springframework.core.annotation.Order
import java.time.LocalDateTime
import java.util.function.Supplier
import kotlin.reflect.KClass

/**
 * 这样能达到在所有Bean初始化之前执行的目的。
 * 继承 BeanFactoryPostProcessor 保证该组件比较早执行。
 */
//@Component
@Order(PriorityOrdered.HIGHEST_PRECEDENCE)
class SpringUtil : BeanDefinitionRegistryPostProcessor, ApplicationContextAware, BeanPostProcessor {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)

        @JvmStatic
        var startAt: LocalDateTime? = null

        private var contextField: ApplicationContext? = null

        @JvmStatic
        val runningInTest: Boolean by lazy {
            return@lazy Thread.currentThread().stackTrace.last().className == "com.intellij.rt.junit.JUnitStarter"
        }

        @JvmStatic
        val binder: Binder by lazy {
            return@lazy Binder.get(context.environment)
        }

        @JvmStatic
        val context: ApplicationContext
            get() {
                if (contextField == null) {
                    throw RuntimeException("SpringUtil 初始化失败!")
                }
                return contextField!!
            }

        private var beanFactoryField: ConfigurableListableBeanFactory? = null

        @JvmStatic
        val beanFactory: ConfigurableListableBeanFactory
            get() {
                if (beanFactoryField == null) {
                    throw RuntimeException("SpringUtil 初始化失败!")
                }
                return beanFactoryField!!
            }

        private var registryField: BeanDefinitionRegistry? = null

        @JvmStatic
        val registry: BeanDefinitionRegistry
            get() {
                if (registryField == null) {
                    throw RuntimeException("SpringUtil 初始化失败!")
                }
                return registryField!!
            }

        /**
         * 动态注册Bean
         */
        @JvmStatic
        @JvmOverloads
        fun registerBeanDefinition(
            name: String,
            instance: Any,
            callback: ((BeanDefinitionBuilder) -> Unit) = {}
        ) {
            registry.registerBeanDefinition(name, getGenericBeanDefinition(instance, callback));

            //添加这一句,就可以执行 Bean 的接口了.
            context.getBean(name, instance::class.java);
        }

        /**
         * 动态注册Bean
         */
        @JvmStatic
        fun registerBeanDefinition(instance: Any) {
            registerBeanDefinition(MyUtil.getSmallCamelCase(instance::class.java.simpleName), instance)
        }


        @JvmStatic
        val isInited: Boolean
            get() = startAt != null

        /**
         * 判断是否存在某个Bean
         */
        @JvmStatic
        @JvmOverloads
        fun containsBean(name: String, clazz: Class<*>, ignoreCase: Boolean = false): Boolean {
            if (name.isEmpty()) return containsBean(clazz)
            return context.getBeanNamesForType(clazz).firstOrNull { it.equals(name, ignoreCase) } != null
        }

        @JvmStatic
        fun containsBean(clazz: KClass<*>): Boolean {
            return containsBean(clazz.java)
        }

        @JvmStatic
        fun containsBean(clazz: Class<*>): Boolean {
            return context.getBeanNamesForType(clazz).any()
        }


        inline fun <reified T> containsBean(): Boolean {
            return this.containsBean(T::class.java);
        }

        @JvmStatic
        fun getBean(name: String): Any {
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
        fun <T : Any> getBean(clazz: KClass<T>): T {
            return getBean(clazz.java)
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

        @JvmStatic
        fun <T : Any> getBeanWithNull(clazz: KClass<T>): T? {
            return getBeanWithNull(clazz.java)
        }

        @JvmStatic
        fun <T> getBeanWithNull(clazz: Class<T>): T? {
            if (containsBean(clazz) == false) return null
            return Companion.context.getBean(clazz);
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
        inline fun <reified T> getBeanWithNull(name: String): T? {
            return getBeanWithNull(name, T::class.java);
        }

        @JvmStatic
        fun <T> getBeanWithNull(name: String, clazz: Class<T>): T? {
            if (containsBean(name, clazz) == false) return null
            return context.getBean(name, clazz);
        }


        /**
         * 动态创建Bean
         */
        @JvmStatic
        fun getGenericBeanDefinition(
            instance: Any,
            callback: ((BeanDefinitionBuilder) -> Unit) = {}
        ): GenericBeanDefinition {
            var type = instance::class.java
            val builder = BeanDefinitionBuilder.genericBeanDefinition(type);

            val definition = builder.rawBeanDefinition as GenericBeanDefinition;
            definition.autowireMode = GenericBeanDefinition.AUTOWIRE_BY_TYPE
            definition.instanceSupplier = Supplier { instance }
            definition.lazyInit = true

            if (type.getAnnotation(Primary::class.java) != null) {
                definition.isPrimary = true;
            }

//            if (instance is InitializingBean) {
//                instance.afterPropertiesSet()
//            }

            callback(builder);
            return definition;
        }
    }

//    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
//        if (inited == false) {
//            inited = true;
//            init_app();
//        }
//
//        return super.postProcessBeforeInitialization(bean, beanName)
//    }


    /**
     * 1. 该方法最早执行。 BeanPostProcessor拦截不到 SpringUtil。
     */
    override fun setApplicationContext(context: ApplicationContext) {
        contextField = context

        if (startAt == null) {
            startAt = LocalDateTime.now()

            this.init_app();
            logger.Important(
                """
~-~-~-~-~-~ SpringUtil初始化! ~-~-~-~-~-~"""
            )
            //发送初始化事件是没用的，因为需要先注册事件，再发出事件。 要保证注册事件在该方法之前
        }
    }

    /**
     * 3. 该方法只执行一次，而且时机很早。
     */
    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        beanFactoryField = beanFactory;
    }

    /**
     * 2. 该方法只执行一次，而且时机很早。
     */
    override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
        registryField = registry;
    }


    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        //记录所有的 ConfigurationProperties
//        if (bean::class.java.annotations.any { ConfigurationProperties::class.java.isAssignableFrom(it.annotationClass.java) }) {
//            logger.info("属性配置项:" + beanName)
//        }

        return super.postProcessAfterInitialization(bean, beanName)
    }

    /**
     * 在所有Bean初始化之前执行
     */
    private fun init_app() {
    }
}


package nbcp.base.utils

import nbcp.base.extend.HasValue
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
            /**
             * 两种方式:
             * 1. 存在Bean：
             * 2. 启动类是： com.intellij.rt.junit.JUnitStarter
             */

//            return@lazy Thread.currentThread().stackTrace.last().className == "com.intellij.rt.junit.JUnitStarter"

            return@lazy context.containsBean("org.springframework.boot.test.context.filter.TestTypeExcludeFilter")
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
            registerBeanDefinition(StringUtil.getSmallCamelCase(instance::class.java.simpleName), instance)
        }


        @JvmStatic
        val isInited: Boolean
            get() = startAt != null

        /**
         * 判断是否存在某个Bean
         */
        @JvmStatic
        @JvmOverloads
        fun containsBean(name: String, type: Class<*>, ignoreCase: Boolean = false): Boolean {
            if (name.isEmpty()) return containsBean(type)
            return context.getBeanNamesForType(type).firstOrNull { it.equals(name, ignoreCase) } != null
        }

        @JvmStatic
        fun containsBean(type: KClass<*>): Boolean {
            return containsBean(type.java)
        }

        @JvmStatic
        fun containsBean(type: Class<*>): Boolean {
            return context.getBeanNamesForType(type).any()
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
        fun <T : Any> getBean(type: KClass<T>): T {
            return getBean(type.java)
        }

        //通过class获取Bean.
        @JvmStatic
        fun <T> getBean(type: Class<T>): T {
            var ret = context.getBean(type);
            return ret;
        }

        @JvmStatic
        inline fun <reified T> getBean(): T {
            return context.getBean(T::class.java);
        }

        @JvmStatic
        fun <T : Any> getBeanWithNull(type: KClass<T>): T? {
            return getBeanWithNull(type.java)
        }

        @JvmStatic
        fun <T> getBeanWithNull(type: Class<T>): T? {
            if (containsBean(type) == false) return null
            return Companion.context.getBean(type);
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
        fun <T> getBeanWithNull(name: String, type: Class<T>): T? {
            if (containsBean(name, type) == false) return null
            return context.getBean(name, type);
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
            logger.Important("""
~~-~~-~~-~~  SpringUtil初始化! ~~-~~-~~-~~ 
"""
            )
            //发送初始化事件是没用的，因为需要先注册事件，再发出事件。 要保证注册事件在该方法之前

            //调试用。 加载配置中心的值。
            var value = context.environment.getProperty("app.config.started-title")
            if (value.HasValue) {
                logger.Important("""
${value}
""");
            }
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


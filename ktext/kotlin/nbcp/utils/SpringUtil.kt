package nbcp.utils

import nbcp.comm.*
import nbcp.component.BaseJsonMapper
import nbcp.component.DbJsonMapper
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.beans.factory.support.GenericBeanDefinition
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.core.PriorityOrdered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import java.util.function.Supplier

/**
 * 这样能达到在所有Bean初始化之前执行的目的。
 * 继承 BeanFactoryPostProcessor 保证该组件比较早执行。
 */
@Component
@Order(PriorityOrdered.HIGHEST_PRECEDENCE)
class SpringUtil : BeanDefinitionRegistryPostProcessor, ApplicationContextAware {
    companion object {
        var startAt: LocalDateTime? = null

        private var contextField: ApplicationContext? = null

        @JvmStatic
        val binder: Binder by lazy {
            return@lazy Binder.get(context.environment)
        }

        @JvmStatic
        val context: ApplicationContext
            get() {
                if (contextField == null) {
                    throw RuntimeException("需要@Import(SpringUtil::class)")
                }
                return contextField!!
            }

        private var beanFactoryField: ConfigurableListableBeanFactory? = null

        @JvmStatic
        val beanFactory: ConfigurableListableBeanFactory
            get() {
                if (beanFactoryField == null) {
                    throw RuntimeException("需要@Import(SpringUtil::class)")
                }
                return beanFactoryField!!
            }

        private var registryField: BeanDefinitionRegistry? = null

        @JvmStatic
        val registry: BeanDefinitionRegistry
            get() {
                if (registryField == null) {
                    throw RuntimeException("需要@Import(SpringUtil::class)")
                }
                return registryField!!
            }

        @JvmStatic
        inline fun <reified T> registerBeanDefinition(
            name: String,
            instance: T,
            callback: ((BeanDefinitionBuilder) -> Unit) = {}
        ) {
            registry.registerBeanDefinition(name, getGenericBeanDefinition(instance, callback));
        }


        @JvmStatic
        val isInited: Boolean
            get() = startAt != null

        /**
         * 判断是否存在某个Bean
         */
        @JvmOverloads
        fun containsBean(name: String, clazz: Class<*>, ignoreCase: Boolean = false): Boolean {
            if (name.isEmpty()) return containsBean(clazz)
            return context.getBeanNamesForType(clazz).firstOrNull { it.equals(name, ignoreCase) } != null
        }

        fun containsBean(clazz: Class<*>): Boolean {
            return context.getBeanNamesForType(clazz).any()
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
        fun <T> getBean(clazz: Class<T>): T {
            var ret = context.getBean(clazz);
            return ret;
        }

        @JvmStatic
        inline fun <reified T> getBean(): T {
            return context.getBean(T::class.java);
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
         * 创建
         */
        inline fun <reified T> getGenericBeanDefinition(
            instance: T,
            callback: ((BeanDefinitionBuilder) -> Unit) = {}
        ): GenericBeanDefinition {
            val builder = BeanDefinitionBuilder.genericBeanDefinition(T::class.java);
            callback(builder);

            val definition = builder.rawBeanDefinition as GenericBeanDefinition;
            definition.autowireMode = GenericBeanDefinition.AUTOWIRE_BY_TYPE
            definition.instanceSupplier = Supplier { instance }
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


    override fun setApplicationContext(context: ApplicationContext) {
        contextField = context

        if (startAt == null) {
            startAt = LocalDateTime.now()

            this.init_app()
        }
    }


    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        beanFactoryField = beanFactory;
    }

    override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
        registryField = registry;
    }


    /**
     * 在所有Bean初始化之前执行
     */
    private fun init_app() {
        DbJsonMapper.addSerializer(MyRawString::class.java, MyRawStringSerializer(), MyRawStringDeserializer())

        BaseJsonMapper.addSerializer(MyString::class.java, MyStringSerializer(), MyStringDeserializer())
        BaseJsonMapper.addSerializer(Date::class.java, DateJsonSerializer(), DateJsonDeserializer())
        BaseJsonMapper.addSerializer(LocalDate::class.java, LocalDateJsonSerializer(), LocalDateJsonDeserializer())
        BaseJsonMapper.addSerializer(LocalTime::class.java, LocalTimeJsonSerializer(), LocalTimeJsonDeserializer())
        BaseJsonMapper.addSerializer(
            LocalDateTime::class.java,
            LocalDateTimeJsonSerializer(),
            LocalDateTimeJsonDeserializer()
        )
        BaseJsonMapper.addSerializer(Timestamp::class.java, TimestampJsonSerializer(), TimestampJsonDeserializer())
    }
}


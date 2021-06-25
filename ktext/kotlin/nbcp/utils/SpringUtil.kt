package nbcp.utils

import nbcp.comm.*
import nbcp.component.BaseJsonMapper
import nbcp.component.DbJsonMapper
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource
import org.springframework.core.env.SimpleCommandLinePropertySource
import org.springframework.stereotype.Component
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

/**
 * 这样能达到在所有Bean初始化之前执行的目的。
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
class SpringUtil : BeanPostProcessor, ApplicationContextAware {
    companion object {
        private var inited = false;
        var startAt: LocalDateTime? = null

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
    }

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {

        if (inited == false) {
            inited = true;
            init_app();
        }

        return super.postProcessBeforeInitialization(bean, beanName)
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

    override fun setApplicationContext(context: ApplicationContext?) {
        if (context == null) {
            throw RuntimeException("设置 ApplicationContext 出现了异常!")
        }

        if (startAt == null) {
            startAt = LocalDateTime.now()
        }
        applicationContext = context
    }
}


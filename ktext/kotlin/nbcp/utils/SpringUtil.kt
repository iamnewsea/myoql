package nbcp.utils

import nbcp.comm.*
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
        DefaultMyJsonMapper.addSerializer(Date::class.java, DateJsonSerializer(), DateJsonDeserializer())
        DefaultMyJsonMapper.addSerializer(LocalDate::class.java, LocalDateJsonSerializer(), LocalDateJsonDeserializer())
        DefaultMyJsonMapper.addSerializer(LocalTime::class.java, LocalTimeJsonSerializer(), LocalTimeJsonDeserializer())
        DefaultMyJsonMapper.addSerializer(LocalDateTime::class.java, LocalDateTimeJsonSerializer(), LocalDateTimeJsonDeserializer())
        DefaultMyJsonMapper.addSerializer(Timestamp::class.java, TimestampJsonSerializer(), TimestampJsonDeserializer())
    }

    override fun setApplicationContext(context: ApplicationContext?) {
        if (context == null) {
            throw RuntimeException("设置 ApplicationContext 出现了异常!")
        }

        applicationContext = context
    }
}


package nbapp

import nbcp.comm.JsonMap
import nbcp.utils.SpringUtil
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.BeanFactoryAware
import org.springframework.beans.factory.BeanNameAware
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.context.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.ContextStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.util.*


@Component
class post1 : BeanPostProcessor {
    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
        println("：：：：${beanName}----${bean.javaClass.name}")

        return super.postProcessBeforeInitialization(bean, beanName)
    }
}

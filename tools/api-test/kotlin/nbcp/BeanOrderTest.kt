package nbcp

import nbcp.comm.JsonMap
import nbcp.utils.SpringUtil
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.BeanFactoryAware
import org.springframework.beans.factory.BeanNameAware
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.AutoConfigureOrder

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConfigurationPropertiesBean
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.context.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.Import
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.ContextStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.util.*
import javax.sql.DataSource

@Component
class NormalBean : InitializingBean {
    override fun afterPropertiesSet() {
        println("6::::InitializingBean")
    }
}

@Configuration
class BeanOrderTest : InitializingBean {
    @Bean
    fun abc(): JsonMap {
        var b = Binder.get(SpringUtil.context.environment)
        var d = b.bind("spring.datasource2", DataSourceProperties::class.java)

        println("5... 内部Bean JsonMap")
        return JsonMap();
    }

    override fun afterPropertiesSet() {
        println("5::::@Configuration")
    }
}

@Component
class BeanNameTest : BeanNameAware {
    override fun setBeanName(name: String) {
        println("4:::::BeanNameAware")
    }
}

@Component
class BeanFactoryAwareTest : BeanFactoryAware {
    override fun setBeanFactory(beanFactory: BeanFactory) {
        println("3:::::BeanFactoryAware")
    }
}

@Component
class ApplicationContextAwareTest : ApplicationContextAware {
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        println("2:::::ApplicationContextAware")
    }
}

@Component
class BeanFactoryPostProcessorTest : BeanFactoryPostProcessor {
    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        println("1 :::::BeanFactoryPostProcessor")
    }
}


@Component
class post1 : BeanPostProcessor {
    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
        if (bean.javaClass == DataSourceAutoConfiguration::class.java ||
            bean.javaClass == EventConfig::class.java ||
            bean.javaClass == RedisAutoConfiguration::class.java
        ) {
            println("::::postProcessBeforeInitialization：${beanName}")
        }


        println("：：：：${beanName}----${bean.javaClass.name}")

        return super.postProcessBeforeInitialization(bean, beanName)
    }
}

@Component
class post333 : ConfigurationPropertiesBindingPostProcessor() {
    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {

//        var d = ConfigurationPropertiesBean.get(SpringUtil.context,Properties(),"ddd");

        var ret = super.postProcessBeforeInitialization(bean, beanName)
        return ret;
    }
}


class MyEvent(var e: String) : ApplicationEvent(Any()) {

}


//@ConditionalOnBean 出现的时机太早了。 要推迟。
@Component
class EventConfig {
    val hasDataSource by lazy {
        return@lazy SpringUtil.containsBean(DataSourceAutoConfiguration::class.java)
    }

    @EventListener
    fun onApplicationEvent(event: MyEvent) {

        if (hasDataSource) {
            event.e = "exist datasource"
        } else {
            event.e = "no datasource"
        }

        println(event.e)
    }

    @EventListener
    fun onE1(event: ContextRefreshedEvent) {
        println("ContextRefreshedEvent")
    }

    @EventListener
    fun onE2(event: ContextRefreshedEvent) {
        println("ContextClosedEvent")
    }

    @EventListener
    fun onE3(event: ContextStartedEvent) {
        println("ContextStartedEvent")
    }
}



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
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.AutoConfigureOrder
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.context.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.Import
import org.springframework.context.event.EventListener
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
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
            bean.javaClass == ExistDatasourceConfig::class.java ||
            bean.javaClass == NotExistDatasourceConfig::class.java
        ) {
            println("::::postProcessBeforeInitialization：${beanName}")
        }

        if (beanName == "dataSource") {
            println("::::postProcessBeforeInitialization：${beanName}")
        }
        return super.postProcessBeforeInitialization(bean, beanName)
    }
}


class MyEvent(var e: String) : ApplicationEvent(Any()) {

}


//@ConditionalOnBean 出现的时机太早了。 要推迟。
@Component
class ExistDatasourceConfig() : BeanDefinitionRegistryPostProcessor {

    public class Ev: ApplicationListener<MyEvent> {
        override fun onApplicationEvent(event: MyEvent) {
            event.e = "exist datasource"
            println(event.e)
        }
    }


    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {

    }

    override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
        if (registry.containsBeanDefinition("dataSource")) {
            registry.registerBeanDefinition("eeeee", SpringUtil.getGenericBeanDefinition(Ev()))
        }
    }
}

@Component
class NotExistDatasourceConfig() : BeanDefinitionRegistryPostProcessor {

    public class Ev: ApplicationListener<MyEvent> {
        override fun onApplicationEvent(event: MyEvent) {
            event.e = "no datasource"
            println(event.e)
        }
    }

    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {

    }

    override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
        if (registry.containsBeanDefinition("dataSource") == false) {
            registry.registerBeanDefinition("eeeee", SpringUtil.getGenericBeanDefinition(Ev()))
        }
    }
}


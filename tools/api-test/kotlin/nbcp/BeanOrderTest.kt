package nbcp

import nbcp.comm.JsonMap
import nbcp.utils.SpringUtil
import org.springframework.beans.factory.BeanNameAware
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.autoconfigure.AutoConfigureOrder
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.ApplicationEvent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.event.EventListener
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component


@Order(Ordered.LOWEST_PRECEDENCE)
@Component
class Com0 : InitializingBean {
    override fun afterPropertiesSet() {
        println("1 ::::LOWEST_PRECEDENCE")
    }
}

@Component
class Com221 : InitializingBean {
    override fun afterPropertiesSet() {
        println("2 ::::InitializingBean")
    }
}


@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
class Com3332 : InitializingBean {
    override fun afterPropertiesSet() {
        println("3 ::::HIGHEST_PRECEDENCE")
    }
}


@Component
class post1 : BeanPostProcessor {
    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
        if (bean.javaClass == DataSourceAutoConfiguration::class.java ||
            bean.javaClass == ExistDatasourceConfig::class.java ||
            bean.javaClass == NoexistDatasourceConfig::class.java
        ) {
            println("::::postProcessBeforeInitialization：${beanName}")
        }
        return super.postProcessBeforeInitialization(bean, beanName)
    }
}


@Component
class nw : BeanNameAware {
    override fun setBeanName(name: String) {
        println("4 :::::BeanNameAware")
    }
}


@Configuration
class BeanOrderTest {
    @Bean
    fun abc(): JsonMap {
        println("5 ::::@Configuration")
        return JsonMap();
    }
}

class MyEvent(var e: String) : ApplicationEvent(Any()) {

}


//@ConditionalOnBean 出现的时机太早了。 要推迟。

@ConditionalOnBean(DataSourceAutoConfiguration::class)
@Order(Ordered.HIGHEST_PRECEDENCE)
@Configuration
class ExistDatasourceConfig :InitializingBean{
    @EventListener

    fun ev(ev: MyEvent) {
        ev.e = "exist datasource"
        println(ev.e)
    }

    override fun afterPropertiesSet() {

    }
}


@ConditionalOnMissingBean(DataSourceAutoConfiguration::class)
@Order(Ordered.HIGHEST_PRECEDENCE)
@Configuration
class NoexistDatasourceConfig :InitializingBean{
    @EventListener

    fun ev(ev: MyEvent) {
        ev.e = "no datasource"
        println(ev.e)
    }

    override fun afterPropertiesSet() {

    }
}
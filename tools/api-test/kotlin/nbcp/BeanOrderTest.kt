package nbcp

import nbcp.comm.JsonMap
import nbcp.utils.SpringUtil
import org.springframework.beans.factory.BeanNameAware
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.AutoConfigureOrder
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.ApplicationEvent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.Import
import org.springframework.context.event.EventListener
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component


@Component
class BeanNameTest : BeanNameAware {
    override fun setBeanName(name: String) {
        println("1 :::::BeanNameAware")
    }
}


@Configuration
class BeanOrderTest : InitializingBean {
    @Bean
    fun abc(): JsonMap {
        println("2... 内部Bean JsonMap")
        return JsonMap();
    }

    override fun afterPropertiesSet() {
        println("2 ::::@Configuration")
    }
}

@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
class HeighestBean : InitializingBean {
    override fun afterPropertiesSet() {
        println("3 ::::HIGHEST_PRECEDENCE")
    }
}

@Order(Ordered.LOWEST_PRECEDENCE)
@Component
class LowestBean : InitializingBean {
    override fun afterPropertiesSet() {
        println("4 ::::LOWEST_PRECEDENCE")
    }
}

@Component
class NormalBean : InitializingBean {
    override fun afterPropertiesSet() {
        println("5 ::::InitializingBean")
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
        return super.postProcessBeforeInitialization(bean, beanName)
    }
}



class MyEvent(var e: String) : ApplicationEvent(Any()) {

}


//@ConditionalOnBean 出现的时机太早了。 要推迟。

@ConditionalOnBean(DataSourceAutoConfiguration::class)
@Configuration
class ExistDatasourceConfig : InitializingBean {

    @EventListener
    fun ev(ev: MyEvent) {
        ev.e = "exist datasource"
        println(ev.e)
    }

    override fun afterPropertiesSet() {
        println(":::: ExistDatasourceConfig")
    }
}


@ConditionalOnMissingBean(DataSourceAutoConfiguration::class)
@Configuration
class NotExistDatasourceConfig : InitializingBean {

    @EventListener
    fun ev(ev: MyEvent) {
        ev.e = "no datasource"
        println(ev.e)
    }

    override fun afterPropertiesSet() {
        println(":::: NotexistDatasourceConfig")
    }
}
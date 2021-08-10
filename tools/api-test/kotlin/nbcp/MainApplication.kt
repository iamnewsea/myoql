package nbcp


import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.web.servlet.ServletComponentScan

import nbcp.comm.*
import nbcp.utils.*
import nbcp.db.*
import org.springframework.beans.factory.InitializingBean

import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration

import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.boot.web.context.WebServerApplicationContext
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.*
import org.springframework.context.event.EventListener
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*





@SpringBootApplication(
    exclude = arrayOf(
        DataSourceAutoConfiguration::class,
        RedisAutoConfiguration::class,
        MongoAutoConfiguration::class,
        RabbitAutoConfiguration::class,
        ElasticsearchDataAutoConfiguration::class
    )
)
@EnableScheduling
@ServletComponentScan
@Import(SpringUtil::class)
//@EnableCircuitBreaker
open class MainApplication {
    companion object {
        val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

//    @Bean
//    fun webSecurityConfigurerAdapter(): WebSecurityConfigurerAdapter {
//        return object : WebSecurityConfigurerAdapter() {
//            override fun configure(httpSecurity: HttpSecurity) {
//                httpSecurity.formLogin().and().csrf().disable()
//            }
//        }
//    }
}


fun main(args: Array<String>) {
//    局显示定义最省事安全
//    Locale.setDefault(Locale.ENGLISH);//推荐用英语地区的算法
    System.setProperty("user.timezone", "Asia/Shanghai");
    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));

//    System.setProperty("logging.file.path", "logs/" + LocalDateTime.now().Format("yyyyMMdd.HHmmss"));
    var context = SpringApplication.run(MainApplication::class.java, *args)


//    db.setDynamicMongo("adminLoginUser", master)
//    db.setDynamicMongo("planInfo", master)
//    db.setDynamicMongo("sysDictionary", master)


    var d = MyEvent("token");
    SpringUtil.context.publishEvent(d);


    println("是否包含 DataSourceAutoConfiguration: "+ SpringUtil.containsBean(DataSourceAutoConfiguration::class.java))
    println("是否包含 DataSourceAutoConfiguration: "+ SpringUtil.context.containsBean(DataSourceAutoConfiguration::class.java.name))

    usingScope(LogScope.info) {
        MainApplication.logger.info(
            MyUtil.getCenterEachLine(
                """
================================================
${context.debugServerInfo}
================================================
""".split("\n")
            )
                .map { ' '.NewString(6) + it }
                .joinToString("\n")
        )
    }

    Thread.sleep(30000)
}

val ApplicationContext.debugServerInfo: String
    get() {
        var list = mutableListOf<String>()
        if (this is WebServerApplicationContext) {
            var port = this.environment.getProperty("server.port")
            list.add("${this.webServer.javaClass.simpleName}:${port}")
        }
        var applicationName = this.environment.getProperty("spring.application.name")
        var version = this.environment.activeProfiles.joinToString(",")


        list.add("${applicationName}:${version}")

        return list.joinToString(" -- ");
    }


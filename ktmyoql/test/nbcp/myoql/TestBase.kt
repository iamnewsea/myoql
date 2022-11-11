package nbcp.myoql

import nbcp.base.annotation.MyLogLevel
import nbcp.base.enums.LogLevelScopeEnum
import nbcp.base.extend.minus
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.Banner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.web.WebAppConfiguration
import java.time.LocalDateTime

/**
 * Created by udi on 17-3-27.
 */


//@SpringBootApplication(exclude = arrayOf(MongoAutoConfiguration::class, RedisAutoConfiguration::class))
@SpringBootApplication(exclude = arrayOf(DataSourceAutoConfiguration::class, RabbitAutoConfiguration::class))
open class KtMyoqlTestApplication {

    companion object {
        @Throws(Exception::class)
        @JvmStatic
        fun main(args: Array<String>) {
            //disabled banner, don't want to see the spring logo
            val app = SpringApplication(KtMyoqlTestApplication::class.java)
            app.setBannerMode(Banner.Mode.OFF)
            var context = app.run(*args)

//            SpringUtil.context = context;
        }
    }
}

@ExtendWith(SpringExtension::class)
@WebAppConfiguration
@SpringBootTest(classes = [KtMyoqlTestApplication::class])
@TestPropertySource(locations = ["classpath:application.yml"])
@MyLogLevel(LogLevelScopeEnum.info)
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
//@ActiveProfiles("unittest","productprofile")
//注释 pom.xml 中的  project.build.resources.resource 中的 excludes
abstract class TestBase {

    init {
//        GenericXmlApplicationContext().environment.setActiveProfiles("test")

//        println(SpringUtil.context.environment.activeProfiles)
        //ParserConfig.getGlobalInstance().putDeserializer(ObjectId::class.java, ObjectIdDeserializer())
    }


    fun execTimes(name: String, times: Int, func: (Int) -> String) {
        println("${name} 执行1000次:")
        val startAt = LocalDateTime.now()
        for (i in 1..times) {
            var ret = func(i)
            if (ret.isEmpty()) continue

            println(ret)
        }
        val endAt = LocalDateTime.now()

        println((endAt - startAt).toString())
    }
}
package nbcp

import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.support.FileSystemXmlApplicationContext
import org.springframework.context.support.GenericXmlApplicationContext
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import nbcp.comm.*
import nbcp.base.utils.SpringUtil
import org.springframework.boot.Banner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.context.annotation.Import
import java.time.Duration
import java.time.LocalDateTime

/**
 * Created by udi on 17-3-27.
 */

@SpringBootApplication(exclude = arrayOf(MongoAutoConfiguration::class, RedisAutoConfiguration::class, DataSourceAutoConfiguration::class))
@Import(SpringUtil::class)
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

@RunWith(SpringRunner::class)
@WebAppConfiguration
@SpringBootTest(classes = arrayOf(KtMyoqlTestApplication::class))
//@TestPropertySource(locations = arrayOf("classpath:application.yml"))
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
        var startAt = LocalDateTime.now()
        for (i in 1..times) {
            var ret = func(i)
            if (ret.isEmpty()) continue

            println(ret)
        }
        var endAt = LocalDateTime.now()

        println((endAt  - startAt).toString())
    }
}
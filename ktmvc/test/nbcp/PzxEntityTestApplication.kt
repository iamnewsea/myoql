package nbcp

import org.springframework.boot.Banner
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Import
import nbcp.base.utils.SpringUtil
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration


@SpringBootApplication
@Import(SpringUtil::class)
@EnableAutoConfiguration(exclude = arrayOf(MongoAutoConfiguration::class, MongoDataAutoConfiguration::class,DataSourceAutoConfiguration::class))
open class PzxEntityTestApplication {

    companion object {
        @Throws(Exception::class)
        @JvmStatic
        fun main(args: Array<String>) {
            //disabled banner, don't want to see the spring logo
            val app = SpringApplication(PzxEntityTestApplication::class.java)
            app.setBannerMode(Banner.Mode.OFF)
            var context = app.run(*args)

//            SpringUtil.context = context;
        }
    }
}
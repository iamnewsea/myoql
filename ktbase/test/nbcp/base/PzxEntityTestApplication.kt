package nbcp.base

import org.springframework.boot.Banner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication


@SpringBootApplication
open class PzxEntityTestApplication {

    companion object {
        @Throws(Exception::class)
        @JvmStatic
        fun main(args: Array<String>) {
            //disabled banner, don't want to see the spring logo
            val app = SpringApplication(PzxEntityTestApplication::class.java)
            app.setBannerMode(Banner.Mode.OFF)
            app.run(*args)

//            SpringUtil.context = context;
        }
    }
}
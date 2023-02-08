package nbcp.base

import org.springframework.boot.Banner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import java.io.BufferedReader
import java.io.Console
import java.io.InputStreamReader


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


            test_ascii()
        }


        fun test_ascii() {
            var startIndex = 0;


            var row = -1;
            while (true) {
                if (row > 0 && row % 100 == 0) {
                    var key = System.`in`.read();
                    if (key == 27) {
                        println("用戶取消！")
                        break
                    }
                }
                row++;
                println("${row * 100 + startIndex}: " + IntRange(row * 100 + startIndex, row * 100 + startIndex + 100).map { Char(it) });
            }
        }
    }
}
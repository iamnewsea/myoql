package nbcp.web


import nbcp.base.extend.minus
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.web.WebAppConfiguration
import java.time.LocalDateTime

/**
 * Created by udi on 17-3-27.
 */


@ExtendWith(SpringExtension::class)
@WebAppConfiguration
@SpringBootTest(classes = [PzxEntityTestApplication::class])
@TestPropertySource(locations = ["classpath:application.yml"])
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

        println( (endAt - startAt).toString())
    }
}
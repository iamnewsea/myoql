package nbcp

import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.web.WebAppConfiguration

/**
 * Created by udi on 17-3-27.
 */


@ExtendWith(SpringExtension::class)
@WebAppConfiguration
@SpringBootTest()
//@TestPropertySource(locations = arrayOf("classpath:application.yml"))
//@ActiveProfiles("unittest","productprofile")
//注释 pom.xml 中的  project.build.resources.resource 中的 excludes
abstract class TestBase {

    init {
        System.setProperty("app.upload.host", "http://dev8.cn")
    }
}
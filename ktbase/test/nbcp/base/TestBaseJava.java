package nbcp.base;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Created by udi on 17-3-27.
 */


@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@SpringBootTest()
@SpringBootApplication
public class TestBaseJava {

    {
        System.setProperty("app.upload.host", "http://dev8.cn");
//        GenericXmlApplicationContext().environment.setActiveProfiles("test")

//        println(SpringUtil.context.environment.activeProfiles)
        //ParserConfig.getGlobalInstance().putDeserializer(ObjectId::class.java, ObjectIdDeserializer())
    }

    public static void main(String... args) {
        SpringApplication app = new SpringApplication(TestBaseJava.class);
        app.run(args);
    }

}
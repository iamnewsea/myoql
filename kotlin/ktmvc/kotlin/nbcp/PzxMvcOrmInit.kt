package nbcp

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.annotation.Configuration

@Configuration
open class PzxMvcOrmInit : ApplicationListener<ContextRefreshedEvent> {


    override fun onApplicationEvent(event: ContextRefreshedEvent) {
    }
}


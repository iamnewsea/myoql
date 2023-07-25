package nbcp.web.feign

import feign.Client
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FeignConfig {

    @Bean
    fun feignClient(
    ): TraceFeignClientDecorator? {
        return TraceFeignClientDecorator(Client.Default(null, null))
    }
}



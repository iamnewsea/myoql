package nbcp.web.feign

import feign.Client
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


class FeignConfig {

    @Bean
    fun traceFeignClient(): TraceFeignClientDecorator {
        return TraceFeignClientDecorator(Client.Default(null, null))
    }
}



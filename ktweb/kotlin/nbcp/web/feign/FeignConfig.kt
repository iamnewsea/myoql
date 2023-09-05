package nbcp.web.feign

import feign.Client
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory
import org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


class FeignConfig {

    @Bean
    fun traceFeignClient(
        cachingFactory: LoadBalancerClient,
        clientFactory: LoadBalancerClientFactory
    ): Client {
        return FeignBlockingLoadBalancerClient(
            TraceFeignClientDecorator(Client.Default(null, null)),
            cachingFactory,
            clientFactory
        )
    }
}



package nbcp.web.fiegn

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping


@FeignClient(name = "docker-feign", url = "\${app.feign.docker.url}")
interface DockerFeignClient {
    @GetMapping("/containers/json")
    fun contains(): String
}
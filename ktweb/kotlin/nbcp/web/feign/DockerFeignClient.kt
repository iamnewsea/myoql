package nbcp.web.feign

import nbcp.base.extend.remove
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

/**
 * Docker Api:
 * https://docs.docker.com/engine/api/v1.42/#section/Authentication
 */
@ConditionalOnProperty("app.feign.docker-api-url")
@FeignClient(name = "docker-feign", url = "\${app.feign.docker-api-url:}")
interface DockerFeignClient {
    class DockerContainer {
        class DockerNetwork {
            var NetworkID = "";
            var Gateway = "";
            var IPAddress = "";
        }

        class DockerNetworkSettings {
            var Networks = mapOf<String, DockerNetwork>()
        }

        class DockerMount {
            var Type = "";
            var Source = "";
            var Destination = "";
            var Propagation = "";
            var RW: Boolean = false;
        }

        var Id = "";
        var Image = "";
        var Created: Long = 0;
        var Names = listOf<String>()
        var State = "";
        var Status = "";
        var NetworkSettings = DockerNetworkSettings();

        var Mounts = listOf<DockerMount>();

        val containerName: String
            get() {
                if (Names.isNullOrEmpty()) {
                    return "";
                }
                return Names.first().remove("/");
            }

        val containerIp: String
            get() {
                return NetworkSettings.Networks.values.firstOrNull()?.IPAddress ?: ""
            }
    }

    @GetMapping("/containers/json")
    fun contains(): List<DockerContainer>

    @PostMapping("/containers/{id}/kill")
    fun kill(@PathVariable id: String)

    @PostMapping("/containers/{id}/start")
    fun start(@PathVariable id: String)


    class ContainerJson {
        var Created = "";
        var State = DockerState();

        class DockerState {
            var Status = "";
            var Running: Boolean? = null;
            var Error: String = "";
            var StartedAt = "";
        }
    }

    @GetMapping("/containers/{id}/json")
    fun inspect(@PathVariable id: String): ContainerJson


    class ContainerAddModel {
        class HostConfigModel {
            var Binds: List<String>? = listOf<String>()
            var Links: List<String>? = listOf<String>()
            var Memory: Int? = 0

            var NanoCpus: Int? = 0

            var CpuShares: Int? = 0
            var CpuPeriod: Int? = 0

            var PortBindings: Map<String, Any?>? = mapOf<String, Any?>()
            var PublishAllPorts: Boolean? = false
            var Privileged: Boolean? = false
            var ReadonlyRootfs: Boolean? = false
            var Dns: List<String>? = listOf<String>()

            var NetworkMode: String? = "bridge"

            //  /dev/shm 目录大小
            var ShmSize: Int? = 0
        }

        var Env: List<String>? = listOf<String>()
        var Cmd: List<String>? = listOf<String>()
        var Entrypoint: String? = ""
        var Image: String? = ""
        var Labels: Map<String, Any?>? = mapOf<String, Any?>()
        var Volumes: Map<String, Any?>? = mapOf<String, Any?>()
        var WorkingDir: String? = ""
        var ExposedPorts: Map<String, Any?>? = mapOf<String, Any?>()
        var HostConfig: HostConfigModel? = HostConfigModel()
    }


    class IdModel {
        var Id: String = "";
    }

    @PostMapping("/containers/create")
    fun add(@RequestBody model: ContainerAddModel): IdModel


    @DeleteMapping("/containers/{id}")
    fun remove(@PathVariable("id") id: String, @RequestParam("force") force: Boolean = true)

}
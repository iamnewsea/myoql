package nbcp.base.service

import nbcp.base.mvc.HttpContext
import nbcp.comm.*
import nbcp.component.AppJsonMapper
import nbcp.component.NacosService
import nbcp.component.SnowFlake
import nbcp.db.db
import nbcp.utils.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestBody
import java.lang.RuntimeException
import java.net.Inet4Address

import java.net.NetworkInterface
import java.time.LocalDateTime


/**
 * https://nacos.io/zh-cn/docs/open-api.html
 */
@Service
open class SnowFlakeRedisService {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    @Autowired
    lateinit var nacosService: NacosService;

    /**
     * 设置雪花算法的机器Id，如果有异常，则设置一个500-1000之间的随机数做为机器Id
     */
    @JvmOverloads
    fun getSnowFlakeMachineId(
        namespaceId: String,
        serviceName: String,
        ip: String,
        port: Int = 80,
        group: String = "DEFAULT_GROUP"
    ): Int {

        val nacosInstancesIpPort =
            nacosService.getNacosInstances("", namespaceId, serviceName, group)
                .map { "${it.ip}:${it.port}" }
                .toMutableList()

        var localUsedIpPort = "${ip}:${port}"

        if (ip.isEmpty()) {
            localUsedIpPort = nacosInstancesIpPort
                .intersect(HttpUtil.localIpAddresses.map { "${it}:${port}" })
                .apply {
                    //随机一个 500-1000之间的id
                    if (this.size != 1) {
                        val machineId = 500 + MyUtil.getRandomWithMaxValue(500);
                        SpringUtil.getBean<SnowFlake>().machineId = machineId;
                        return machineId
                    }
                }
                .first();
        }

        if (nacosInstancesIpPort.contains(localUsedIpPort) == false) {
            nacosInstancesIpPort.add(localUsedIpPort);
        }


        val appName = namespaceId + "-" + serviceName;
        //第一次初始化应用。
        val redisInstances = db.rer_base.nacosInstance.getMap(appName)
            .mapValues { it.value.AsInt() }
            .toMutableMap()


        if (redisInstances.isEmpty() || !redisInstances.containsKey(localUsedIpPort)) {
            redisInstances.put(localUsedIpPort, 0);
        }

        fillMachineId(redisInstances);
        var machineId = redisInstances.get(localUsedIpPort).AsInt();
        //先设置到自己。
        if (machineId == 0) {
            machineId = 500 + MyUtil.getRandomWithMaxValue(500);
        }

        SpringUtil.getBean<SnowFlake>().machineId = machineId;

        db.rer_base.nacosInstance.resetMap(appName, redisInstances);
        return machineId;
    }


    /**
     * 填充Id
     */
    private fun fillMachineId(redisNacosInstanceNewData: MutableMap<String, Int>) {
        var usedId = redisNacosInstanceNewData.values.toSet();

        redisNacosInstanceNewData.filter { it.value == 0 }.keys.forEach { ipPort ->
            var randomId = 0;
            (1..3).forEach {
                randomId = 100 + MyUtil.getRandomWithMaxValue(400);
                if (usedId.contains(randomId) == false) {
                    return@forEach
                }
            }

            redisNacosInstanceNewData.put(ipPort, randomId);
        }
    }
}
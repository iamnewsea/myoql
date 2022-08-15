package nbcp.base.mvc.service

import nbcp.comm.*
import nbcp.component.SnowFlake
import nbcp.db.db
import nbcp.utils.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service


/**
 * https://nacos.io/zh-cn/docs/open-api.html
 */
@Service
open class SnowFlakeRedisService {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    val nacosService = NacosServiceUtil;

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
                        val machineId = MyUtil.getRandomNumber(500, 1000);
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
        val redisInstances = db.rerBase.nacosInstance(appName).getMap()
            .mapValues { it.value.AsInt() }
            .toMutableMap()


        if (redisInstances.isEmpty() || !redisInstances.containsKey(localUsedIpPort)) {
            redisInstances.put(localUsedIpPort, 0);
        }

        fillMachineId(redisInstances);
        var machineId = redisInstances.get(localUsedIpPort).AsInt();
        //先设置到自己。
        if (machineId == 0) {
            machineId = MyUtil.getRandomNumber(500, 1000);
        }

        SpringUtil.getBean<SnowFlake>().machineId = machineId;

        db.rerBase.nacosInstance(appName).resetMap(redisInstances);
        return machineId;
    }


    /**
     * 填充Id
     */
    private fun fillMachineId(redisNacosInstanceNewData: MutableMap<String, Int>) {
        var usedId = redisNacosInstanceNewData.values.toSet();

        redisNacosInstanceNewData.filter { it.value == 0 }.keys.forEach { ipPort ->
            var randomId = 0;
            (1..3).forEach for1@{
                randomId = MyUtil.getRandomNumber(100, 500);
                if (usedId.contains(randomId) == false) {
                    return@for1
                }
            }

            redisNacosInstanceNewData.put(ipPort, randomId);
        }
    }
}
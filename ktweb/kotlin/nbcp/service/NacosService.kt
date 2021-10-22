package nbcp.service

import nbcp.comm.*
import nbcp.component.AppJsonMapper
import nbcp.component.SnowFlake
import nbcp.db.db
import nbcp.utils.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Service
import java.lang.RuntimeException
import java.net.Inet4Address

import java.net.NetworkInterface


/**
 * https://nacos.io/zh-cn/docs/open-api.html
 */
@Service
open class NacosService {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    @Value("\${spring.cloud.nacos.discovery.server-addr:}")
    lateinit var nacosServerAddress: String

    val serverHost: String
        get() {
            if (nacosServerAddress.isEmpty()) {
                throw RuntimeException("需要指定配置项 spring.cloud.nacos.discovery.server-addr")
            }
            var ret = nacosServerAddress
            if (!ret.startsWith("http://", true) && !ret.startsWith("https://", true)) {
                ret = "http://${ret}"
            }

            if (ret.endsWith("/")) {
                ret = ret.substring(0, ret.length - 2);
            }

            if (ret.endsWith("/nacos") == false) {
                ret = ret + "/nacos"
            }

            return ret;
        }

    /**
     * 获取配置项
     */
    @JvmOverloads
    fun getConfig(namespaceId: String, dataId: String, group: String = "DEFAULT_GROUP"): ApiResult<String> {
        val query = StringMap();
        query["dataId"] = dataId;
        query["group"] = group;
        query["tenant"] = namespaceId;

        val http = HttpUtil("$serverHost/v1/cs/configs?${query.toUrlQuery()}")
        val res = http.doGet();
        if (http.status == 200) {
            return ApiResult.of(res)
        } else {
            return ApiResult("ns:$namespaceId,dataId:$dataId,group:$group , 获取nacos配置错误 : $res")
        }
    }

    /**
     * 设置配置项
     */
    @JvmOverloads
    fun setConfig(
        namespaceId: String,
        dataId: String,
        configContent: String,
        group: String = "DEFAULT_GROUP",
        type: String = "yaml",
    ): JsonResult {
        val http = HttpUtil("$serverHost/v1/cs/configs")
        val query = StringMap();
        query["dataId"] = dataId;
        query["group"] = group;
        query["tenant"] = namespaceId;
        query["content"] = configContent;
        query["type"] = type.AsString("yaml");

        val res = http.doPost(query.toUrlQuery())

        if (http.status == 200) {
            return JsonResult()
        } else {
            return JsonResult("ns:$namespaceId,dataId:$dataId,group:$group , 发布nacos错误 : $res")
        }
    }

    data class NacosInstanceHostData @JvmOverloads constructor(
        var ip: String = "",
        var port: Int = 0,
        var valid: Boolean = false,
        var healthy: Boolean = false,
        var marked: Boolean = false,
        var instanceId: String = "",
        var metadata: StringMap = StringMap(),
        var enabled: Boolean = false,
        var weight: Int = 0,
        var clusterName: String = "",
        var serviceName: String = "",
        var ephemeral: Boolean = false
    )

    data class NacosInstanceData @JvmOverloads constructor(
        var hosts: MutableList<NacosInstanceHostData> = mutableListOf(),
        var dom: String = "",
        var name: String = "",
        var cacheMillis: Int = 0,
        var lastRefTime: Long = 0L,
        var checksum: String = "",
        var clusters: String = "",
        var env: String = "",
        var metadata: StringMap = StringMap()
    )

    /**
     * 获取Nacos实例列表。
     */
    @JvmOverloads
    fun getNacosInstances(
        namespaceId: String,
        serviceName: String,
        group: String = "DEFAULT_GROUP"
    ): MutableSet<String> {
        val query = StringMap();
        query["serviceName"] = serviceName;
        query["groupName"] = group;
        query["namespaceId"] = namespaceId;

        val http = HttpUtil("$serverHost/v1/ns/instance/list?${query.toUrlQuery()}")
        val res = http.doGet();
        if (http.status != 200) {
            throw RuntimeException("ns:$namespaceId,dataId:$serviceName,group:$group , 获取nacos实例错误 : $res")
        }

        return res.FromJson<NacosInstanceData>()!!.hosts.map { it.ip + ":" + it.port }.toMutableSet()
    }

    fun getIpAddresses(): List<String> {
        val allNetInterfaces = NetworkInterface.getNetworkInterfaces()
        var ips = mutableListOf<String>()
        while (allNetInterfaces.hasMoreElements()) {
            val netInterface = allNetInterfaces.nextElement() as NetworkInterface
            if (netInterface.isLoopback || netInterface.isVirtual || !netInterface.isUp) {
                continue
            }

            val addresses = netInterface.inetAddresses
            while (addresses.hasMoreElements()) {
                val ip = addresses.nextElement()
                if (ip != null && ip is Inet4Address) {
                    ips.add(ip.getHostAddress())
                }
            }
        }
        return ips;
    }

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

        val nacosInstancesIpPort = getNacosInstances(namespaceId, serviceName, group)
        var localUsedIpPort = "${ip}:${port}"

        if (ip.isEmpty()) {
            localUsedIpPort = nacosInstancesIpPort.intersect(getIpAddresses().map { it + ":" + port })
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
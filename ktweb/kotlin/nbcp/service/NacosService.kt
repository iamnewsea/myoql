package nbcp.service

import nbcp.comm.*
import nbcp.component.SnowFlake
import nbcp.db.db
import nbcp.utils.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
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
    var nacosServerAddress: String = "http://127.0.0.1:8848/nacos"

    val serverHost: String
        get() {
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
        val group = group.AsString("DEFAULT_GROUP")
        var query = StringMap();
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
        var group = group.AsString("DEFAULT_GROUP")

        val http = HttpUtil("$serverHost/v1/cs/configs")
        var query = StringMap();
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

    data class NacosInstanceHostData(
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

    data class NacosInstanceData(
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
    fun getInstances(
        namespaceId: String,
        serviceName: String,
        group: String = "DEFAULT_GROUP"
    ): ApiResult<NacosInstanceData> {
        var group = group.AsString("DEFAULT_GROUP")
        var query = StringMap();
        query["serviceName"] = serviceName;
        query["groupName"] = group;
        query["namespaceId"] = namespaceId;

        val http = HttpUtil("$serverHost/v1/ns/instance/list?${query.toUrlQuery()}")
        val res = http.doGet();
        if (http.status == 200) {
            return ApiResult.of(res.FromJson<NacosInstanceData>())
        } else {
            return ApiResult("ns:$namespaceId,dataId:$serviceName,group:$group , 获取nacos实例错误 : $res")
        }
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
                var ip = addresses.nextElement()
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
    fun setSnowFlakeMachineId(
        namespaceId: String,
        serviceName: String,
        group: String = "DEFAULT_GROUP"
    ): ApiResult<Int> {
        var appName = namespaceId + "-" + serviceName;
        var nacosInstances = getInstances(namespaceId, serviceName, group)
            .apply {
                //随机一个 500-1000之间的id
                SpringUtil.getBean<SnowFlake>().machineId = 500 + MyUtil.getRandomWithMaxValue(500);
                if (this.msg.HasValue) return ApiResult(this.msg);
            }.data!!
            .hosts

        var nacosInstancesWithIp = nacosInstances.map { it.ip }.toTypedArray();
        var localUsedIp = nacosInstancesWithIp.intersect(getIpAddresses())
            .apply {
                //随机一个 500-1000之间的id
                SpringUtil.getBean<SnowFlake>().machineId = 500 + MyUtil.getRandomWithMaxValue(500);
                if (this.size == 0) {
                    return ApiResult("未找到注册到 nacos 的实例，nacosip:${nacosInstancesWithIp.joinToString()}")
                }
                if (this.size > 1) {
                    return ApiResult("找到多个本地使用的Ip:${this.joinToString(",")}")
                }
            }
            .first();


        //第一次初始化应用。
        var redisInstances = db.rer_base.nacosInstance.getMap(appName)
            .mapValuesTo(mutableMapOf<String, Int>(), { it.value.AsInt() })

        if (redisInstances.isEmpty() || !redisInstances.containsKey(localUsedIp)) {
            redisInstances.put(localUsedIp, 0);
        }

        var redisInstancesNewData = redisInstances.filter { nacosInstancesWithIp.contains(it.key) }.toMutableMap()

        fillNacosNewData(redisInstancesNewData);
        var machineId = redisInstancesNewData.get(localUsedIp).AsInt();
        //先设置到自己。
        if (machineId > 0) {
            SpringUtil.getBean<SnowFlake>().machineId = machineId;
        }

        if (redisInstancesNewData.EqualMapContent(redisInstances)) {
            return ApiResult.of(machineId);
        }

        db.rer_base.nacosInstance.resetMap(appName, redisInstancesNewData);
        return ApiResult.of(machineId);
    }


    /**
     * 填充Id
     */
    private fun fillNacosNewData(redisNacosInstanceNewData: MutableMap<String, Int>) {
        //每10位的最小值。
        var ids_set = redisNacosInstanceNewData.values
            .groupBy { it.AsInt() / 10 }
            .map { it.value.minOrNull()!! }
            .filter { it > 0 }


        if (redisNacosInstanceNewData.size != ids_set.size) {
            redisNacosInstanceNewData.keys.forEach { ip ->
                var id = redisNacosInstanceNewData[ip];
                if (ids_set.contains(id) == false) {
                    redisNacosInstanceNewData.set(ip, 0)
                }
            }
        }

        var ary = (1..9).toList().toMutableSet();
        redisNacosInstanceNewData.keys.forEach { ip ->
            var id = redisNacosInstanceNewData[ip]!! / 10;
            if (id > 0) {
                ary.remove(id);
            } else {
                if (ary.size == 0) {
                    //一个应用有大于9个实例？ 随机分配一个100到200之间的机器号
                    redisNacosInstanceNewData.put(ip, 100 + MyUtil.getRandomWithMaxValue(100));
                    return@forEach
                }

                var randomIndex = MyUtil.getRandomWithMaxValue(ary.size);
                var randomId = ary.elementAt(randomIndex);
                ary.remove(randomId);

                var randomNumber = MyUtil.getRandomWithMaxValue(10);
                redisNacosInstanceNewData.put(ip, randomId * 10 + randomNumber);
            }
        }

    }

}
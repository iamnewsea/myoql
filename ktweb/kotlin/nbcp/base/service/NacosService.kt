package nbcp.base.service

import nbcp.base.mvc.HttpContext
import nbcp.comm.*
import nbcp.component.AppJsonMapper
import nbcp.component.SnowFlake
import nbcp.db.db
import nbcp.utils.*
import org.slf4j.LoggerFactory
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
open class NacosService {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    data class NacosConfigItemData(
        var id: String = "",
        var dataId: String = "",
        var group: String = "",
        var content: String = "",
        var tenant: String = "",
        var type: String? = null //模糊查找时为空。
    )

    data class NacosConfigsResponseDataModel(
        var totalCount: Int = 0,
        var pageNumber: Int = 0,
        var pagesAvailable: Int = 0,
        var pageItems: Array<NacosConfigItemData> = arrayOf()
    )

    fun listConfigs(
        serverHost: String,
        ns: String,
        group: String,
        dataId: String
    ): ListResult<NacosConfigItemData> {
        return queryConfigs(serverHost, ns, group, dataId, 1)
    }

    private fun queryConfigs(
        serverHost: String,
        ns: String,
        group: String,
        dataId: String,
        pageNumber: Int = 1
    ): ListResult<NacosConfigItemData> {
        val group = group.AsString("DEFAULT_GROUP")
        var searchType = "blur"; // blur： 模糊，  accurate ：精确
        val http =
            HttpUtil("${getServerFullHost(serverHost)}/v1/cs/configs?dataId=${dataId.AsString("*")}&group=${group}&tenant=$ns&pageNo=${pageNumber}&pageSize=100&search=${searchType}")
        val res = http.doGet();
        if (http.status != 200) {
            return ListResult("ns:$ns,dataId:$dataId,group:$group , 获取nacos配置错误 : $res")
        }
        var list = mutableListOf<NacosConfigItemData>()
        var data = res.FromJson<NacosConfigsResponseDataModel>()!!;
        list.addAll(data.pageItems)

        if (data.pagesAvailable > data.pageNumber) {
            var dataNext = queryConfigs(serverHost, ns, group, dataId, pageNumber + 1);
            if (dataNext.msg.HasValue) {
                return ListResult(dataNext.msg);
            }
            list.addAll(dataNext.data)
        }

        return ListResult.of(list)
    }

    fun getConfig(serverHost: String, ns: String, group: String, dataId: String): ApiResult<String> {
        val group = group.AsString("DEFAULT_GROUP")
        val http = HttpUtil("${getServerFullHost(serverHost)}/v1/cs/configs?dataId=${dataId}&group=${group}&tenant=$ns")
        val res = http.doGet();
        if (http.status == 200) {
            return ApiResult.of(res)
        }
        return ApiResult.error("ns:$ns,dataId:$dataId,group:$group , 获取nacos配置错误 : $res")
    }

    fun setConfig(
        serverHost: String,
        ns: String,
        group: String,
        dataId: String,
        type: String,
        content: String
    ): JsonResult {

        val http = HttpUtil("${getServerFullHost(serverHost)}/v1/cs/configs")
        val res =
            http.doPost(
                "dataId=$dataId&group=${group.AsString("DEFAULT_GROUP")}&tenant=$ns&content=${
                    JsUtil.encodeURIComponent(
                        content
                    )
                }&type=${type.AsString("yaml")}"
            )

        if (http.status == 200) {
            return JsonResult()
        } else {
            HttpContext.response.status = 500;
            return JsonResult.error("ns:$ns,dataId:$dataId,group:${group.AsString("DEFAULT_GROUP")} , 发布nacos错误 : $res")
        }
    }


    fun setGateway(
        serverHost: String,
        ns: String,
        app_name: String,
        author: String,
        data_id: String
    ): JsonResult {
        var group = "DEFAULT_GROUP"

        var sign = "#--[template]--#"
        var start_sign = "#start [${app_name}]"
        var end_sign = "#end [${app_name}]"
        var space_count = -1;

        var template = """
${start_sign}
# generate by ${author} at ${LocalDateTime.now().AsString()}
- id: ${app_name}
  uri: lb://${app_name}
  predicates:
    - Path=/${app_name}/**
  filters:
    - RewritePath=/${app_name}/(?<segment>.*), /$\{segment}
${end_sign}
"""
        var ret1 = getConfig(serverHost, ns, group, data_id);
        if (ret1.msg.HasValue) {
            return JsonResult.error(ret1.msg);
        }
        var content = ret1.data!!

        var lines = content.split("\n").toMutableList();

        var startIndex = lines.indexOfFirst { it.contains(start_sign) };
        var endIndex = lines.indexOfLast { it.contains(end_sign) };


        if ((startIndex >= 0) xor (endIndex >= 0)) {
            return JsonResult.error("数据模板不匹配")
        }

        if (startIndex >= 0 && endIndex >= 0) {
            lines =
                (lines.take(startIndex).trimEmptyLine() + const.line_break + lines.takeLast(lines.size - endIndex - 1)
                    .trimEmptyLine()).toMutableList()
        }

        for (i in lines.indices) {
            var line = lines[i];
            if (line.contains("\t")) {
                return JsonResult.error("在 ${i}行中包含 tab 字符！")
            }

            if (space_count < 0) {
                space_count = line.indexOf(sign)
            }

            if (space_count < 0) {
                continue;
            }

            template = template.prependIndent(" ".Repeat(space_count))

            lines.add(i + 1, template);
            break;
        }

        var ret = setConfig(serverHost, ns, group, data_id, "", lines.joinToString("\n"));
        if (ret.msg.HasValue) {
            return JsonResult.error(ret.msg);
        }
        return JsonResult();
    }

    private fun List<String>.trimEmptyLine(): List<String> {
        if (this.any() == false) return listOf();
        var startCount = -1;
        var endCount = -1;
        this.any {
            startCount++;
            return@any it.count { it.isLetterOrDigit() } > 0;
        }

        this.asReversed().any {
            endCount++;
            return@any it.count { it.isLetterOrDigit() } > 0;
        }

        return this.subList(startCount, this.size - endCount);
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

    private fun getServerFullHost(host: String): String {
        var ret = host;
        if (ret.isEmpty()) {
            ret = SpringUtil.context.environment.getProperty("spring.cloud.nacos.config.server-addr").AsString();
        }
        if (!ret.startsWith("http://", true) && !ret.startsWith("https://", true)) {
            if (!ret.contains(":")) {
                ret = ret + ":8848"
            }

            ret = "http://${ret}"
        }

        if (ret.endsWith("/")) {
            ret = ret.substring(0, ret.length - 1);
        }

        if (ret.endsWith("/nacos") == false) {
            ret = ret + "/nacos"
        }
        return ret;
    }

    private val serverHost: String by lazy {
        return@lazy getServerFullHost("")
    }

    /**
     * 获取Nacos实例列表。
     */
    @JvmOverloads
    fun getNacosInstances(
        serverHost: String,
        namespaceId: String,
        serviceName: String,
        group: String = "DEFAULT_GROUP"
    ): MutableSet<String> {
        val query = StringMap();
        query["serviceName"] = serviceName;
        query["groupName"] = group;
        query["namespaceId"] = namespaceId;

        val http = HttpUtil("${getServerFullHost(serverHost)}/v1/ns/instance/list?${query.toUrlQuery()}")
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

        val nacosInstancesIpPort = getNacosInstances(serverHost, namespaceId, serviceName, group)
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